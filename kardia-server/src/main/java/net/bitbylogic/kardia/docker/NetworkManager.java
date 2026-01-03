package net.bitbylogic.kardia.docker;

import com.github.dockerjava.api.DockerClient;
import com.github.dockerjava.api.command.BuildImageResultCallback;
import com.github.dockerjava.api.command.CreateContainerResponse;
import com.github.dockerjava.api.model.*;
import com.github.dockerjava.core.DefaultDockerClientConfig;
import com.github.dockerjava.core.DockerClientBuilder;
import com.github.dockerjava.core.DockerClientConfig;
import com.github.dockerjava.httpclient5.ApacheDockerHttpClient;
import com.github.dockerjava.transport.DockerHttpClient;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import net.bitbylogic.kardia.Kardia;
import net.bitbylogic.kardia.docker.container.GenericKardiaContainer;
import net.bitbylogic.kardia.docker.container.KardiaContainer;
import net.bitbylogic.kardia.docker.file.FileConstants;
import net.bitbylogic.kardia.server.KardiaServer;
import net.bitbylogic.kardia.server.ServerType;
import net.bitbylogic.kardia.util.Callback;
import net.bitbylogic.kardia.util.RedisKeys;
import org.redisson.api.RMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.time.Duration;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;
import java.util.stream.Collectors;

public class NetworkManager {

    private final File rootDirectory = new File(Paths.get("").toUri());

    private final Map<String, String> imageCache = new ConcurrentHashMap<>();
    private final Cache<String, DockerPackage> packageCache = CacheBuilder.newBuilder().build();

    private final List<KardiaServer> activeServers = new CopyOnWriteArrayList<>();
    private final List<KardiaContainer> activeContainers = new CopyOnWriteArrayList<>();

    private final ScheduledExecutorService serverUpdater = Executors.newSingleThreadScheduledExecutor();
    private final ScheduledExecutorService serverManager = Executors.newSingleThreadScheduledExecutor();

    private final ExecutorService containerService = Executors.newFixedThreadPool(4);

    private final DockerHttpClient httpClient;
    private final DockerClient dockerClient;

    public NetworkManager() {
        String dockerHost = System.getenv("DOCKER_HOST");

        if (dockerHost == null) {
            dockerHost = Kardia.config().getString("Docker.Host", "tcp://localhost:2375");
        }

        DockerClientConfig config = DefaultDockerClientConfig.createDefaultConfigBuilder()
                .withDockerHost(dockerHost)
                .build();

        this.httpClient = new ApacheDockerHttpClient.Builder()
                .dockerHost(config.getDockerHost())
                .sslConfig(config.getSSLConfig())
                .connectionTimeout(Duration.ofSeconds(10))
                .responseTimeout(Duration.ofSeconds(30))
                .build();

        this.dockerClient = DockerClientBuilder.getInstance(config).withDockerHttpClient(httpClient).build();

        setup();
    }

    private void setup() {
        Arrays.stream(FileConstants.values()).forEach(file -> {
            File f = new File(rootDirectory.getAbsolutePath() + file.getRelativePath());

            if (f.exists()) {
                return;
            }

            switch (file.getFileType()) {
                case DIRECTORY:
                    boolean dirMade = f.mkdir();

                    if (!dirMade) {
                        Kardia.LOGGER.error("Failed to create directory {}!", f.getAbsolutePath());
                    }

                    break;
                case FILE:
                    try {
                        boolean fileMade = f.createNewFile();

                        if(!fileMade) {
                            Kardia.LOGGER.error("Failed to create file {}!", f.getAbsolutePath());
                        }
                    } catch (IOException e) {
                        Kardia.LOGGER.error("Failed to create file {}!", f.getAbsolutePath(), e);
                    }
                    break;
                default:
                    break;
            }
        });

        cacheImages();
        loadContainers();

        loadAllPackages();
        loadMissingPackages();

        startUpdaterThread();
        startManagerThread();
    }

    private void cacheImages() {
        imageCache.clear();

        dockerClient.listImagesCmd().exec().forEach(image -> {
            if (image.getRepoTags() == null || image.getRepoTags().length == 0 || !image.getRepoTags()[0].startsWith(KardiaServer.PREFIX)) {
                return;
            }

            String[] tag = image.getRepoTags()[0].split(":");

            imageCache.put(tag[0], image.getId());
        });

        Kardia.LOGGER.info("Cached {} image(s) from Docker.", imageCache.size());
    }

    private void loadContainers() {
        List<Container> containers = dockerClient.listContainersCmd().withShowAll(true).exec();
        List<String> invalidateIds = Lists.newArrayList();

        containers.forEach(container -> {
            if (isValidName(container)) {
                if (container.getStatus().toLowerCase().startsWith("active")) {
                    String id = container.getId();
                    String kardiaId = container.getNames()[0].replaceFirst("/", "");
                    int port = container.getPorts().length >= 1 && container.getPorts()[0].getPublicPort() != null ?
                            container.getPorts()[0].getPublicPort() : 0;

                    this.activeContainers.add(new GenericKardiaContainer(id, kardiaId, port));
                    return;
                }

                invalidateIds.add(container.getId());
            }
        });

        if (!activeContainers.isEmpty()) {
            Kardia.LOGGER.info("Loaded {} active container(s).", activeContainers.size());
        }

        if (!invalidateIds.isEmpty()) {
            invalidateIds.forEach(id -> dockerClient.removeContainerCmd(id).exec());
            Kardia.LOGGER.warn("Invalidated {} outdated container(s) from Docker!", invalidateIds.size());
        }
    }

    private void loadMissingPackages() {
        AtomicInteger missingPackages = new AtomicInteger(0);

        this.packageCache.asMap().forEach((_, dockerPackage) -> {
            if (imageCache.containsKey(dockerPackage.getImageKey())) {
                return;
            }

            missingPackages.incrementAndGet();

            createImage(dockerPackage).thenRun(() -> {
                Kardia.LOGGER.info("Created image for package {} ({})", dockerPackage.instance(), dockerPackage.ids().getFirst());

                int packagesLeft = missingPackages.decrementAndGet();

                if (packagesLeft == 0) {
                    Kardia.LOGGER.info("Finished building images for missing package(s).");
                }
            });
        });

        if(missingPackages.get() <= 0) {
            return;
        }

        Kardia.LOGGER.info("Building images for missing package(s)...");
    }

    private void startUpdaterThread() {
        this.serverUpdater.scheduleAtFixedRate(() -> {
            try {
                List<Container> fetchedContainers = dockerClient.listContainersCmd().exec();

                Set<String> fetchedIds = fetchedContainers.stream()
                        .map(Container::getId)
                        .collect(Collectors.toSet());

                activeContainers.removeIf(container -> {
                    boolean exists = fetchedIds.contains(container.dockerId());

                    if (!exists) {
                        KardiaServer server = getServerByKardiaID(container.kardiaId());

                        if (server != null) {
                            activeServers.remove(server);
                        }
                    }

                    return !exists;
                });

                for (Container container : fetchedContainers) {
                    if (container.getNames().length == 0 || !isValidName(container)) {
                        continue;
                    }

                    String status = container.getStatus().toLowerCase();
                    if (!status.contains("up") && !status.contains("restarting")) {
                        dockerClient.removeContainerCmd(container.getId())
                                .withForce(true)
                                .exec();

                        KardiaContainer matched = getContainerByDockerId(container.getId());

                        if (matched != null) {
                            activeContainers.remove(matched);
                            KardiaServer server = getServerByKardiaID(matched.kardiaId());

                            if (server != null) {
                                activeServers.remove(server);
                            }
                        }

                        Kardia.serverManager().unregisterServer(container.getNames()[0]);
                    }
                }

                RMap<String, String> allServers = Kardia.redisClient().getRedisClient().getMap(RedisKeys.SERVERS);

                List<String> invalidIds = new ArrayList<>();
                List<KardiaServer> validServers = new ArrayList<>();

                for (Map.Entry<String, String> entry : allServers.entrySet()) {
                    KardiaServer server = Kardia.gson().fromJson(entry.getValue(), KardiaServer.class);

                    if (getContainerByKardiaId(server.kardiaId()) != null) {
                        validServers.add(server);
                        continue;
                    }

                    invalidIds.add(entry.getKey());
                }

                if (!invalidIds.isEmpty()) {
                    allServers.fastRemove(invalidIds.toArray(new String[0]));
                }

                this.activeServers.clear();
                this.activeServers.addAll(validServers);

                if(invalidIds.isEmpty()) {
                    return;
                }

                Kardia.LOGGER.info("Invalidated {} container(s) from Redis!", invalidIds.size());
            } catch (Exception e) {
                Kardia.LOGGER.error("Failed to update containers!", e);
            }
        }, 0, 30, TimeUnit.SECONDS);
    }

    private void startManagerThread() {
        this.serverManager.scheduleAtFixedRate(() -> {
            packageCache.asMap().forEach((_, dockerPackage) -> {
                String imageKey = dockerPackage.getImageKey();

                if (!imageCache.containsKey(imageKey)) {
                    return;
                }

                List<KardiaServer> servers = getServersById(dockerPackage.ids().getFirst());
                int currentCount = servers.size();
                int requiredCount = dockerPackage.cache();

                if (currentCount >= requiredCount) {
                    return;
                }

                String pkgId = dockerPackage.ids().getFirst();
                Kardia.LOGGER.info(
                        "Starting package {} to meet cache requirement ({}/{})",
                        pkgId, currentCount, requiredCount
                );

                Logger packageLogger = LoggerFactory.getLogger(pkgId);

                startPackage(dockerPackage, new Callback<>() {
                    @Override
                    public void info(String message) {
                        packageLogger.info(message);
                    }

                    @Override
                    public void error(String message) {
                        packageLogger.error(message);
                    }
                });
            });
        }, 0, 10, TimeUnit.SECONDS);
    }

    public void shutdown() {
        this.serverUpdater.shutdown();
        this.serverManager.shutdown();
        this.containerService.shutdown();
        this.packageCache.invalidateAll();

        try {
            try (DockerClient dockerClient = this.dockerClient) {
                activeContainers.parallelStream().forEach(container -> {
                    dockerClient.removeContainerCmd(container.dockerId())
                            .withForce(true)
                            .exec();
                });
            } catch (IOException e) {
                Kardia.LOGGER.error("Failed to remove containers!", e);
            }

            httpClient.close();
        } catch (IOException e) {
            Kardia.LOGGER.error("Failed to close HTTP client!", e);
        }
    }

    public File getFile(FileConstants constant) {
        return new File(this.rootDirectory.getAbsolutePath() + constant.getRelativePath());
    }

    public DockerPackage getPackage(String id) {
        return packageCache.getIfPresent(id);
    }

    public KardiaContainer getContainerByDockerId(String dockerId) {
        return this.activeContainers.stream()
                .filter(container -> container.dockerId().equals(dockerId))
                .findFirst()
                .orElse(null);
    }

    public KardiaContainer getContainerByKardiaId(String kardiaId) {
        return this.activeContainers.stream()
                .filter(container -> container.kardiaId().equals(kardiaId))
                .findFirst()
                .orElse(null);
    }

    public KardiaContainer getContainerByPort(int port) {
        return this.activeContainers.stream()
                .filter(container -> container.boundPort() == port)
                .findFirst()
                .orElse(null);
    }

    public KardiaServer getServerByKardiaID(String kardiaId) {
        return this.activeServers.stream()
                .filter(server -> server.kardiaId().equals(kardiaId))
                .findFirst()
                .orElse(null);
    }

    public List<KardiaServer> getServersByInstance(String instance) {
        return this.activeServers.stream()
                .filter(server -> server.instance().equals(instance))
                .collect(Collectors.toList());
    }

    public List<KardiaServer> getServersById(String id) {
        return this.activeServers.stream()
                .filter(server -> server.ids().contains(id))
                .collect(Collectors.toList());
    }

    public boolean isValidName(Container container) {
        if (container.getNames().length < 1) {
            return false;
        }

        for (ServerType type : ServerType.values()) {
            if(container.getNames()[0].substring(1).startsWith(type.getPrefix())) {
                return true;
            }
        }

        return false;
    }

    public void loadPackage(String instance) {
        try {
            File mainPkgDirectory = getFile(FileConstants.PACKAGES_DIRECTORY);
            File pkgDirectory = new File(mainPkgDirectory.getAbsolutePath() + File.separatorChar + instance);

            if (!pkgDirectory.exists() || !pkgDirectory.isDirectory()) {
                Kardia.LOGGER.warn("Package {} does not exist!", instance);
                return;
            }

            File[] files = pkgDirectory.listFiles();

            if (files == null) {
                Kardia.LOGGER.warn("Package {} does not contain any files!", instance);
                return;
            }

            for (File file : files) {
                if(file.isDirectory() || !file.getName().equalsIgnoreCase("package.json")) {
                    continue;
                }

                String json = Files.readString(file.toPath());

                DockerPackage dockerPackage = Kardia.gson().fromJson(json, DockerPackage.class);
                dockerPackage.setInstance(instance);

                packageCache.put(instance, dockerPackage);
            }
        } catch (Exception e) {
            Kardia.LOGGER.error("Failed to load package {}!", instance, e);
        }
    }

    public void loadAllPackages() {
        File[] files = getFile(FileConstants.PACKAGES_DIRECTORY).listFiles();

        if (files == null) {
            return;
        }

        for (File file : files) {
            if(!file.isDirectory()) {
                continue;
            }

            loadPackage(file.getName());
        }

        Kardia.LOGGER.info("Loaded {} package(s).", packageCache.size());
    }

    public String newKardiaId(DockerPackage pkg) {
        String id;

        do {
            id = String.format("%s-%s", pkg.serverType().getPrefix(), getServersByInstance(pkg.instance()).size() + 1);
        } while (getContainerByKardiaId(id) != null);

        return id;
    }

    public int newBoundPort() {
        int minPort = Kardia.config().getInt("Port-Range.Min", 27000);
        int maxPort = Kardia.config().getInt("Port-Range.Max", 30000);

        if (activeContainers.size() >= (maxPort - minPort))
            throw new StackOverflowError("Active container(s) exceed port range limits");

        ThreadLocalRandom random = ThreadLocalRandom.current();

        int port;

        do {
            port = minPort + random.nextInt(maxPort - minPort + 1);
        } while (getContainerByPort(port) != null);

        return port;
    }

    public int newBoundPort(DockerPackage pkg) {
        if (!pkg.isPortRangeValid()) {
            return newBoundPort();
        }

        ThreadLocalRandom random = ThreadLocalRandom.current();

        int port;

        do {
            port = pkg.portMin() + random.nextInt(pkg.portMax() - pkg.portMin() + 1);
        } while (getContainerByPort(port) != null);

        return port;
    }

    public CompletableFuture<String> createImage(DockerPackage dockerPackage) {
        CompletableFuture<String> imageIdCompletable = new CompletableFuture<>();

        File instanceDirectory = new File(
                getFile(FileConstants.PACKAGES_DIRECTORY),
                dockerPackage.instance()
        );

        List<String> dockerfile = new ArrayList<>();
        Consumer<String> line = dockerfile::add;

        line.accept("FROM eclipse-temurin:25-jre-alpine");
        line.accept("ARG SERVER_ROOT=stomarcade/");

        dockerPackage.envVars().forEach(v -> line.accept("ENV " + v));

        line.accept("RUN apk add --no-cache unzip \\");
        line.accept("    && mkdir -p $SERVER_ROOT");

        line.accept("ADD resources/" +
                dockerPackage.serverType().getJarName() +
                " $SERVER_ROOT");

        dockerPackage.serverType()
                .getConfigFiles()
                .forEach(f -> line.accept("ADD resources/" + f + " $SERVER_ROOT"));

        if (dockerPackage.world() != null) {
            line.accept("ADD packages/" +
                    instanceDirectory.getName() + "/" +
                    dockerPackage.world() +
                    " $SERVER_ROOT");
        }

        Optional.ofNullable(dockerPackage.root()).ifPresent(rootName -> {
            File root = new File(instanceDirectory, rootName);

            if (root.exists()) {
                line.accept("ADD packages/" +
                        instanceDirectory.getName() + "/" +
                        rootName +
                        " $SERVER_ROOT");
            } else {
                Kardia.LOGGER.error(
                        "Root for instance {}, package {} is specified but does not exist",
                        instanceDirectory.getName(),
                        dockerPackage.ids().getFirst()
                );
            }
        });

        dockerPackage.serverType().getRequiredConfigs().forEach(c ->
                line.accept("ADD packages/" +
                        instanceDirectory.getName() + "/" +
                        c +
                        " $SERVER_ROOT")
        );

        line.accept("WORKDIR $SERVER_ROOT");

        List<String> zipFiles = dockerPackage.serverType().getRequiredConfigs().stream()
                .filter(c -> c.endsWith(".zip"))
                .toList();

        if (!zipFiles.isEmpty()) {
            line.accept("RUN " + zipFiles.stream()
                    .map(f -> "unzip " + f + " && rm " + f)
                    .collect(Collectors.joining(" && ")));
        }

        String cmd =
                "java " +
                        dockerPackage.serverType().getCommandLineArguments() +
                        " -jar " +
                        dockerPackage.serverType().getJarName() +
                        Optional.ofNullable(dockerPackage.serverType().getPostCommandLineArguments())
                                .filter(s -> !s.isBlank())
                                .map(s -> " " + s.trim())
                                .orElse("");

        line.accept("CMD " + cmd);
        line.accept("EXPOSE 25565");

        File file = new File(rootDirectory.getAbsolutePath() + File.separatorChar +
                "dockerfile_" + dockerPackage.instance() + "_" + dockerPackage.ids().getFirst());

        try {
            Files.write(file.toPath(), dockerfile, StandardOpenOption.CREATE);
        } catch (IOException e) {
            Kardia.LOGGER.error("Failed to write Dockerfile!", e);
        }

        CompletableFuture.runAsync(() -> {
            Kardia.LOGGER.info("Building image for package {}...", dockerPackage.instance());

            AtomicBoolean started = new AtomicBoolean(false);
            String imageKey = dockerPackage.getImageKey();

            dockerClient.buildImageCmd()
                    .withTags(Sets.newHashSet(imageKey))
                    .withDockerfile(file)
                    .exec(new BuildImageResultCallback() {
                        @Override
                        public void onNext(BuildResponseItem item) {
                            if (started.compareAndSet(false, true)) {
                                Kardia.LOGGER.info("Building image for {}...", dockerPackage.instance());
                            }

                            if (item.getErrorDetail() != null) {
                                Kardia.LOGGER.error("Error building image for {}: {}", dockerPackage.instance(), item.getErrorDetail().getMessage());
                            }

                            if (item.isBuildSuccessIndicated()) {
                                Kardia.LOGGER.info("Successfully built image for {}!", dockerPackage.instance());
                                String id = item.getImageId();

                                imageCache.remove(imageKey);
                                imageCache.put(imageKey, id);

                                try {
                                    Files.delete(file.toPath());
                                } catch (IOException e) {
                                    Kardia.LOGGER.error("Failed to delete Dockerfile!", e);
                                }

                                imageIdCompletable.complete(id);
                            }

                            String stream = item.getStream();

                            if (stream != null && !stream.isBlank()) {
                                Kardia.LOGGER.info(stream);
                            }
                        }
                    });
        });

        return imageIdCompletable;
    }

    public synchronized void startPackage(DockerPackage pkg, Callback<String> callback) {
        String imageKey = pkg.getImageKey();

        if (!imageCache.containsKey(imageKey) || imageCache.get(imageKey) == null) {
            callback.error("Image for package does not exist.");
            return;
        }

        Ports portBindings = new Ports();
        int boundPort = newBoundPort(pkg);
        portBindings.bind(ExposedPort.tcp(25565), Ports.Binding.bindPort(boundPort));

        String imageId = imageCache.getOrDefault(imageKey, null);
        String kardiaId = newKardiaId(pkg);

        List<String> envVars = Lists.newArrayList(
                "KARDIA_ID=" + kardiaId,
                "KARDIA_IP=none",
                "KARDIA_BOUND_PORT=" + boundPort,
                "KARDIA_INSTANCE_NAME=" + pkg.instance() + ":" + pkg.ids().getFirst(),
                "KARDIA_PRIVATE_SERVER=" + pkg.isPrivateServer(),
                "SQL_HOST=" + (pkg.sqlHost() == null ? Kardia.config().getString("Server.SQL.Host", "localhost") : pkg.sqlHost()),
                "SQL_DATABASE=" + (pkg.sqlDatabase() == null ? Kardia.config().getString("Server.SQL.Database", "test") : pkg.sqlDatabase()),
                "SQL_PORT=" + (pkg.sqlPort() == 0 ? Kardia.config().getInt("Server.SQL.Port", 3306) : pkg.sqlPort()),
                "SQL_USERNAME=" + (pkg.sqlUsername() == null ? Kardia.config().getString("Server.SQL.Username", "root") : pkg.sqlUsername()),
                "SQL_PASSWORD=" + (pkg.sqlPassword() == null ? Kardia.config().getString("Server.SQL.Password", "") : pkg.sqlPassword()),
                "REDIS_HOST=" + (pkg.redisHost() == null ? Kardia.config().getString("Server.Redis.Host", "localhost") : pkg.redisHost()),
                "REDIS_PASSWORD=" + (pkg.redisPassword() == null ? Kardia.config().getString("Server.Redis.Password", "") : pkg.redisPassword()),
                "REDIS_PORT=" + (pkg.redisPort() == 0 ? Kardia.config().getInt("Server.Redis.Port", 6379) : pkg.redisPort()),
                "REDIS_SOURCE_ID=" + kardiaId
        );

        containerService.submit(() -> {
            try {
                HostConfig hostConfig = new HostConfig()
                        .withPortBindings(portBindings)
                        .withNetworkMode("bridge")
                        .withRestartPolicy(RestartPolicy.alwaysRestart());

                CreateContainerResponse response = dockerClient.createContainerCmd(imageId)
                        .withHostConfig(hostConfig)
                        .withEnv(envVars)
                        .withName(kardiaId)
                        .exec();

                dockerClient.startContainerCmd(response.getId()).exec();

                KardiaContainer container = new GenericKardiaContainer(response.getId(), kardiaId, boundPort);
                activeContainers.add(container);

                KardiaServer server = Kardia.serverManager().registerServer(
                        kardiaId,
                        pkg.instance(),
                        pkg.ids(),
                        Kardia.config().getString("Servers-IP", "localhost"),
                        boundPort,
                        pkg.serverType()
                );

                activeServers.add(server);

                callback.info("Started container with ID: " + container.kardiaId());
            } catch (Exception e) {
                callback.error("An error occurred while starting the server: " + e.getMessage());
            }
        });
    }

    /**
     * Stops a container, removing it from the cache.
     *
     * @param container The container to stop and remove.
     * @return A {@link CompletableFuture} returning whether the server was
     * stopped or not.
     */
    public synchronized CompletableFuture<Boolean> stopContainer(KardiaContainer container) {
        CompletableFuture<Boolean> completed = new CompletableFuture<>();
        containerService.submit(() -> {
            try {
                dockerClient.removeContainerCmd(container.dockerId())
                        .withForce(true)
                        .exec();
                completed.complete(true);

                activeContainers.remove(container);
            } catch (Exception e) {
                Kardia.LOGGER.error("Failed to stop container!", e);
            }
        });
        return completed;
    }

    public synchronized CompletableFuture<Boolean> stopServer(KardiaServer server) {
        CompletableFuture<Boolean> completed;
        KardiaContainer container = getContainerByKardiaId(server.kardiaId());
        if (container != null) {
            completed = stopContainer(container);
        } else {
            completed = new CompletableFuture<>();
            completed.complete(false);
        }
        return completed;
    }

    public DockerClient dockerClient() {
        return dockerClient;
    }

    public List<KardiaContainer> activeContainers() {
        return List.copyOf(activeContainers);
    }

    public List<KardiaServer> activeServers() {
        return List.copyOf(activeServers);
    }

    public Cache<String, DockerPackage> packageCache() {
        Cache<String, DockerPackage> tempCache = CacheBuilder.newBuilder().build();
        tempCache.putAll(packageCache.asMap());

        return tempCache;
    }

}
