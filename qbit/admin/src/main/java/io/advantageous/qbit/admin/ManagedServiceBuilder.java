package io.advantageous.qbit.admin;

import io.advantageous.boon.core.Str;
import io.advantageous.boon.core.Sys;
import io.advantageous.consul.discovery.ConsulServiceDiscoveryBuilder;
import io.advantageous.qbit.Factory;
import io.advantageous.qbit.QBit;
import io.advantageous.qbit.annotation.AnnotationUtils;
import io.advantageous.qbit.events.EventManager;
import io.advantageous.qbit.http.server.HttpServerBuilder;
import io.advantageous.qbit.meta.builder.ContextMetaBuilder;
import io.advantageous.qbit.metrics.support.LocalStatsCollectorBuilder;
import io.advantageous.qbit.metrics.support.StatServiceBuilder;
import io.advantageous.qbit.metrics.support.StatsDReplicatorBuilder;
import io.advantageous.qbit.server.EndpointServerBuilder;
import io.advantageous.qbit.service.ServiceBuilder;
import io.advantageous.qbit.service.ServiceBundleBuilder;
import io.advantageous.qbit.service.discovery.ServiceDiscovery;
import io.advantageous.qbit.service.health.HealthServiceAsync;
import io.advantageous.qbit.service.health.HealthServiceBuilder;
import io.advantageous.qbit.system.QBitSystemManager;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

/** This is a utility class for when you are running in a PaaS like Heroku or Docker.
 *  It also allows you to share stat, health and system manager setup.
 *
 * If statsD is enabled then this will look for the statsd port and host from STATSD_PORT and STATSD_HOST environment
 * variables.
 *
 *
 * If you use the admin builder, the port for the admin will be from the environment variable
 * QBIT_ADMIN_PORT. It can be overridden from system properties as well see AdminBuilder for more details.
 *
 *
 * The main end point port will be read from the environment variable WEB_PORT and if not found then read from
 * PORT.
 *
 * Defaults for ports and hosts can be overridden by their respective builders.
 **/
public class ManagedServiceBuilder {


    /** Holds the system manager used to register proper shutdown with JVM. */
    private QBitSystemManager systemManager;

    /** Holds the stats service builder used to collect system stats. */
    private StatServiceBuilder statServiceBuilder;

    /** EndpointServerBuilder used to hold the main EndpointServerBuilder. */
    private EndpointServerBuilder endpointServerBuilder;

    /** Used to create LocalStatsCollection. */
    private LocalStatsCollectorBuilder localStatsCollectorBuilder;

    /** Used to create the main http server. */
    private HttpServerBuilder httpServerBuilder;

    /** Enables local stats collection. */
    private boolean enableLocalStats=true;

    /** Enables sending stats to stats D. */
    private boolean enableStatsD=false;

    /** Enables local health stats collection. */
    private boolean enableLocalHealth=true;

    /** Used to create local health collector. */
    private HealthServiceBuilder healthServiceBuilder;

    /** Health service used to collect health info from service queues. */
    private HealthServiceAsync healthService;

    /** Enables the collection of stats. */
    private boolean enableStats = true;

    /** Event manager for services, service queues, and end point servers. */
    private EventManager eventManager;

    /** Factory to create QBit parts. */
    private Factory factory;

    /** Builder to hold context information about endpoints. */
    private ContextMetaBuilder contextMetaBuilder;

    /** Endpoint services that will be exposed through contextMetaBuilder. */
    private List<Object> endpointServices;

    /** The builder for the admin. */
    private AdminBuilder adminBuilder;

    /** StatsD replicator builder. */
    private StatsDReplicatorBuilder statsDReplicatorBuilder;


    /** Service Discovery. */
    private ServiceDiscovery serviceDiscovery;

    /** Service Discovery supplier. */
    private Supplier<ServiceDiscovery> serviceDiscoverySupplier;


    /** Default Root URI.  */
    private  String rootURI = Sys.sysProp(ManagedServiceBuilder.class.getName()
            + ".rootURI", "/services");

    /** Default public host used for swagger. */
    private  String publicHost = Sys.sysProp(ManagedServiceBuilder.class.getName()
            + ".publicHost", "localhost");

    /** Actual port. */
    private int port = Sys.sysProp(ManagedServiceBuilder.class.getName()
            + ".port", 8080);

    /** Public port used for swagger. */
    private int publicPort = Sys.sysProp(ManagedServiceBuilder.class.getName()
            + ".publicPort", -1);


    /** How often stats should be flushed. */
    private int sampleStatFlushRate = Sys.sysProp(ManagedServiceBuilder.class.getName()
            + ".sampleStatFlushRate", 5);

    /** How often timings should be collected defaults to every 100 calls. */
    private int checkTimingEveryXCalls = Sys.sysProp(ManagedServiceBuilder.class.getName()
            + ".checkTimingEveryXCalls", 100);


    public ManagedServiceBuilder(String serviceName) {
        if (serviceName != null) {


            final MicroserviceConfig config = MicroserviceConfig.readConfig(serviceName);

            /** Configure ManagedServiceBuilder. */
            this.setRootURI(config.getRootURI());
            this.setPublicHost(config.getPublicHost());
            this.setPublicPort(config.getPublicPort());
            this.setPort(config.getPort());


            /** Configure ManagedServiceBuilder. */
            final ContextMetaBuilder contextMetaBuilder = this.getContextMetaBuilder();
            contextMetaBuilder.setLicenseName(config.getLicenseName());
            contextMetaBuilder.setLicenseURL(config.getLicenseURL());
            contextMetaBuilder.setContactURL(config.getContactURL());
            contextMetaBuilder.setTitle(config.getTitle());
            contextMetaBuilder.setVersion(config.getVersion());
            contextMetaBuilder.setContactName(config.getContactName());
            contextMetaBuilder.setContactEmail(config.getContactEmail());
            contextMetaBuilder.setDescription(config.getDescription());

            /** Configure statsD. */
            if (config.isStatsD()) {
                this.setEnableStatsD(true);
                this.getStatsDReplicatorBuilder().setHost(config.getStatsDHost());

                if (config.getStatsDPort() != -1) {
                    this.getStatsDReplicatorBuilder().setPort(config.getStatsDPort());
                }
            }

            this.setCheckTimingEveryXCalls(config.getCheckTimingEveryXCalls());
            this.setSampleStatFlushRate(config.getSampleStatFlushRate());

            this.setEnableLocalStats(config.isEnableLocalStats());
            this.setEnableStats(config.isEnableStats());
            this.setEnableLocalHealth(config.isEnableLocalHealth());
        }
    }

    public String getPublicHost() {

        if (System.getenv("PUBLIC_HOST")!=null) {
            publicHost = System.getenv("PUBLIC_HOST");
        }
        return publicHost;
    }

    public ManagedServiceBuilder setPublicHost(String publicHost) {
        this.publicHost = publicHost;
        return this;
    }

    public int getPublicPort() {
        if (publicPort==-1) {

            String sport = System.getenv("PUBLIC_WEB_PORT");

            if (!Str.isEmpty(sport)) {
                publicPort = Integer.parseInt(sport);
            }
        }

        if (publicPort==-1) {
            publicPort = getPort();
        }

        return publicPort;
    }

    public ManagedServiceBuilder setPublicPort(int publicPort) {
        this.publicPort = publicPort;
        return this;
    }

    public int getPort() {

        if (port==8080) {

            String sport = System.getenv("PORT_WEB");
            if (Str.isEmpty(sport)) {
                sport = System.getenv("PORT");
            }


            if (!Str.isEmpty(sport)) {
                port = Integer.parseInt(sport);
            }
        }
        return port;
    }

    public ManagedServiceBuilder setPort(int port) {
        this.port = port;
        return this;
    }

    public String getRootURI() {
        return rootURI;
    }

    public ManagedServiceBuilder setRootURI(String rootURI) {
        this.rootURI = rootURI;
        return this;
    }

    public void enableConsulServiceDiscovery(String dataCenter) {
        final ConsulServiceDiscoveryBuilder consulServiceDiscoveryBuilder = ConsulServiceDiscoveryBuilder.consulServiceDiscoveryBuilder();
        consulServiceDiscoveryBuilder.setDatacenter(dataCenter);
        serviceDiscoverySupplier = () -> consulServiceDiscoveryBuilder.build();
    }

    public void enableConsulServiceDiscovery(String dataCenter, String host) {
        final ConsulServiceDiscoveryBuilder consulServiceDiscoveryBuilder = ConsulServiceDiscoveryBuilder.consulServiceDiscoveryBuilder();
        consulServiceDiscoveryBuilder.setDatacenter(dataCenter);
        consulServiceDiscoveryBuilder.setConsulHost(host);
        serviceDiscoverySupplier = () -> consulServiceDiscoveryBuilder.build();
    }


    public void enableConsulServiceDiscovery(String dataCenter, String host, int port) {
        final ConsulServiceDiscoveryBuilder consulServiceDiscoveryBuilder = ConsulServiceDiscoveryBuilder.consulServiceDiscoveryBuilder();
        consulServiceDiscoveryBuilder.setDatacenter(dataCenter);
        consulServiceDiscoveryBuilder.setConsulHost(host);
        consulServiceDiscoveryBuilder.setConsulPort(port);
        serviceDiscoverySupplier = () -> consulServiceDiscoveryBuilder.build();
    }

    public Supplier<ServiceDiscovery> getServiceDiscoverySupplier() {
        return serviceDiscoverySupplier;
    }

    public ManagedServiceBuilder setServiceDiscoverySupplier(Supplier<ServiceDiscovery> serviceDiscoverySupplier) {
        this.serviceDiscoverySupplier = serviceDiscoverySupplier;
        return this;
    }

    public ServiceDiscovery getServiceDiscovery() {
        if (serviceDiscovery == null) {
            if (serviceDiscoverySupplier!=null) {
                serviceDiscovery = serviceDiscoverySupplier.get();
            }
        }
        return serviceDiscovery;
    }

    public ManagedServiceBuilder setServiceDiscovery(ServiceDiscovery serviceDiscovery) {
        this.serviceDiscovery = serviceDiscovery;
        return this;
    }

    public StatsDReplicatorBuilder getStatsDReplicatorBuilder() {
        if (statsDReplicatorBuilder == null) {
            statsDReplicatorBuilder = StatsDReplicatorBuilder.statsDReplicatorBuilder();

            final String statsDPort = System.getenv("STATSD_PORT");

            if (statsDPort!=null && !statsDPort.isEmpty()) {
                statsDReplicatorBuilder.setPort(Integer.parseInt(statsDPort));
            }

            final String statsDHost = System.getenv("STATSD_HOST");


            if (statsDHost!=null && !statsDHost.isEmpty()) {
                statsDReplicatorBuilder.setHost(statsDHost);
            }

        }
        return statsDReplicatorBuilder;
    }

    public ManagedServiceBuilder setStatsDReplicatorBuilder(
            final StatsDReplicatorBuilder statsDReplicatorBuilder) {
        this.statsDReplicatorBuilder = statsDReplicatorBuilder;
        return this;
    }

    public String findAdminPort() {
        String qbitAdminPort = getAdminPort("QBIT_ADMIN_PORT");
        if (Str.isEmpty(qbitAdminPort)) {
            qbitAdminPort = getAdminPort("PORT_ADMIN");
        }
        return qbitAdminPort;
    }

    public AdminBuilder getAdminBuilder() {
        if (adminBuilder == null) {
            adminBuilder = AdminBuilder.adminBuilder();

            final String qbitAdminPort = findAdminPort();
            if (qbitAdminPort!=null && !qbitAdminPort.isEmpty()) {
                adminBuilder.setPort(Integer.parseInt(qbitAdminPort));
            }
            adminBuilder.setContextBuilder(this.getContextMetaBuilder());
            adminBuilder.setHealthService(getHealthService());
            adminBuilder.registerJavaVMStatsJob(getStatServiceBuilder().buildStatsCollector());

        }
        return adminBuilder;
    }

    private String getAdminPort(String qbit_admin_port) {
        return System.getenv(qbit_admin_port);
    }


    public ManagedServiceBuilder setAdminBuilder(AdminBuilder adminBuilder) {
        this.adminBuilder = adminBuilder;
        return this;
    }

    public ContextMetaBuilder getContextMetaBuilder() {
        if (contextMetaBuilder == null) {
            contextMetaBuilder = ContextMetaBuilder.contextMetaBuilder();
            contextMetaBuilder
                    .setHostAddress(this.getPublicHost() + ":" + this.getPublicPort())
                    .setRootURI(this.getRootURI());
        }
        return contextMetaBuilder;
    }

    public ManagedServiceBuilder setContextMetaBuilder(final ContextMetaBuilder contextMetaBuilder) {
        this.contextMetaBuilder = contextMetaBuilder;
        return this;
    }

    public List<Object> getEndpointServices() {
        if (endpointServices == null) {
            endpointServices = new ArrayList<>();
        }

        return endpointServices;
    }

    public ManagedServiceBuilder setEndpointServices(final List<Object> endpointServices) {
        this.endpointServices = endpointServices;
        return this;
    }


    public ManagedServiceBuilder addEndpointService(final Object endpointService) {
        getContextMetaBuilder().addService(endpointService.getClass());
        getEndpointServices().add(endpointService);
        return this;
    }

    public Factory getFactory() {
        if (factory == null) {
            factory = QBit.factory();
        }
        return factory;
    }

    public ManagedServiceBuilder setFactory(final Factory factory) {
        this.factory = factory;
        return this;
    }

    public EventManager getEventManager() {
        if (eventManager == null) {
            eventManager = getFactory().systemEventManager();
        }
        return eventManager;
    }

    public ManagedServiceBuilder setEventManager(EventManager eventManager) {
        this.eventManager = eventManager;
        return this;
    }

    public ManagedServiceBuilder setHealthServiceBuilder(final HealthServiceBuilder healthServiceBuilder) {
        this.healthServiceBuilder = healthServiceBuilder;
        return this;
    }

    public HealthServiceBuilder getHealthServiceBuilder() {

        if (healthServiceBuilder == null) {
            healthServiceBuilder = HealthServiceBuilder.healthServiceBuilder();
        }
        return healthServiceBuilder;
    }

    public static ManagedServiceBuilder managedServiceBuilder() {
        return new ManagedServiceBuilder(null);
    }


    public static ManagedServiceBuilder managedServiceBuilder(final String serviceName) {
        return new ManagedServiceBuilder(serviceName);
    }

    public HttpServerBuilder getHttpServerBuilder() {
        if (httpServerBuilder == null) {
            httpServerBuilder = HttpServerBuilder.httpServerBuilder();

            int port = getPort();


            if (!Str.isEmpty(port)) {
                httpServerBuilder.setPort(port);
            }
        }
        return httpServerBuilder;
    }

    public ManagedServiceBuilder setHttpServerBuilder(HttpServerBuilder httpServerBuilder) {
        this.httpServerBuilder = httpServerBuilder;
        return this;
    }

    public boolean isEnableStatsD() {
        return enableStatsD;
    }

    public ManagedServiceBuilder setEnableStatsD(boolean enableStatsD) {
        this.enableStatsD = enableStatsD;
        return this;
    }

    public LocalStatsCollectorBuilder getLocalStatsCollectorBuilder() {
        if (localStatsCollectorBuilder==null) {
            localStatsCollectorBuilder = LocalStatsCollectorBuilder.localStatsCollectorBuilder();
        }
        return localStatsCollectorBuilder;
    }

    public ManagedServiceBuilder setLocalStatsCollectorBuilder(LocalStatsCollectorBuilder localStatsCollectorBuilder) {
        this.localStatsCollectorBuilder = localStatsCollectorBuilder;
        return this;
    }

    public boolean isEnableLocalStats() {
        return enableLocalStats;
    }

    public ManagedServiceBuilder setEnableLocalStats(boolean enableLocalStats) {
        this.enableLocalStats = enableLocalStats;
        return this;
    }

    public boolean isEnableLocalHealth() {
        return enableLocalHealth;
    }

    public ManagedServiceBuilder setEnableLocalHealth(boolean enableLocalHealth) {
        this.enableLocalHealth = enableLocalHealth;
        return this;
    }

    public QBitSystemManager getSystemManager() {

        if (systemManager == null) {
            systemManager = new QBitSystemManager();
        }
        return systemManager;
    }

    public ManagedServiceBuilder setSystemManager(QBitSystemManager systemManager) {
        this.systemManager = systemManager;
        return this;
    }



    public StatServiceBuilder getStatServiceBuilder() {
        if (statServiceBuilder == null) {
            statServiceBuilder =  StatServiceBuilder.statServiceBuilder();

            if (enableLocalStats) {
                statServiceBuilder.addReplicator(getLocalStatsCollectorBuilder().buildAndStart());
            }

            if (enableStatsD) {
                statServiceBuilder.addReplicator(getStatsDReplicatorBuilder().buildAndStart());
            }

            statServiceBuilder.build();
            statServiceBuilder.buildServiceQueueWithCallbackHandler()
                    .startCallBackHandler().start();

        }
        return statServiceBuilder;
    }

    public ManagedServiceBuilder setStatServiceBuilder(StatServiceBuilder statServiceBuilder) {

        this.statServiceBuilder = statServiceBuilder;
        return this;
    }

    public EndpointServerBuilder getEndpointServerBuilder() {
        if (endpointServerBuilder==null) {
            endpointServerBuilder = EndpointServerBuilder.endpointServerBuilder();
            endpointServerBuilder.setPort(this.getPort());
            endpointServerBuilder.setEnableHealthEndpoint(isEnableLocalHealth());
            endpointServerBuilder.setEnableStatEndpoint(isEnableLocalStats());

            endpointServerBuilder.setHealthService(getHealthService());
            endpointServerBuilder.setSystemManager(this.getSystemManager());
            endpointServerBuilder.setHttpServerBuilder(getHttpServerBuilder());
            endpointServerBuilder.setStatsFlushRateSeconds(getSampleStatFlushRate());
            endpointServerBuilder.setCheckTimingEveryXCalls(getCheckTimingEveryXCalls());
            endpointServerBuilder.setServiceDiscovery(getServiceDiscovery());
            endpointServerBuilder.setUri(getRootURI());



            if (isEnableStats()) {

                endpointServerBuilder.setStatsCollector(getStatServiceBuilder().buildStatsCollectorWithAutoFlush());
            }

            if (isEnableLocalStats()) {
                endpointServerBuilder.setEnableStatEndpoint(true);
                endpointServerBuilder.setStatsCollection(getLocalStatsCollectorBuilder().build());
            }

            endpointServerBuilder.setupHealthAndStats(getHttpServerBuilder());

            if (endpointServices != null) {
                endpointServerBuilder.setServices(endpointServices);
            }

        }

        return endpointServerBuilder;
    }


    public EndpointServerBuilder createEndpointServerBuilder() {

        EndpointServerBuilder endpointServerBuilder = EndpointServerBuilder.endpointServerBuilder();
        endpointServerBuilder.setEnableHealthEndpoint(isEnableLocalHealth());
        endpointServerBuilder.setSystemManager(this.getSystemManager());
        endpointServerBuilder.setHealthService(getHealthService());
        endpointServerBuilder.setStatsFlushRateSeconds(getSampleStatFlushRate());
        endpointServerBuilder.setCheckTimingEveryXCalls(getCheckTimingEveryXCalls());
        endpointServerBuilder.setServiceDiscovery(getServiceDiscovery());



        if (isEnableStats()) {

            endpointServerBuilder.setStatsCollector(getStatServiceBuilder().buildStatsCollectorWithAutoFlush());
        }

        if (isEnableLocalStats()) {
                endpointServerBuilder.setEnableStatEndpoint(true);
                endpointServerBuilder.setStatsCollection(getLocalStatsCollectorBuilder().build());
        }

        return endpointServerBuilder;
    }


    public ServiceBundleBuilder createServiceBundleBuilder() {

        ServiceBundleBuilder serviceBundleBuilder = ServiceBundleBuilder.serviceBundleBuilder();
        serviceBundleBuilder.setSystemManager(this.getSystemManager());
        serviceBundleBuilder.setHealthService(getHealthService());
        serviceBundleBuilder.setCheckTimingEveryXCalls(this.getCheckTimingEveryXCalls());
        serviceBundleBuilder.setStatsFlushRateSeconds(this.getSampleStatFlushRate());


        if (isEnableStats()) {
            serviceBundleBuilder.setStatsCollector(getStatServiceBuilder().buildStatsCollector());
        }

        return serviceBundleBuilder;
    }


    public ServiceBuilder createServiceBuilderForServiceObject(final Object serviceObject) {

        ServiceBuilder serviceBuilder = ServiceBuilder.serviceBuilder();
        serviceBuilder.setSystemManager(this.getSystemManager());

        serviceBuilder.setServiceObject(serviceObject);

        final String bindStatHealthName =  AnnotationUtils.readServiceName(serviceObject);

        if (isEnableLocalHealth()) {
            serviceBuilder.registerHealthChecks(getHealthService(), bindStatHealthName);
        }


        if (isEnableStats()) {


            serviceBuilder.registerStatsCollections(bindStatHealthName,
                    getStatServiceBuilder().buildStatsCollector(), getSampleStatFlushRate(), getCheckTimingEveryXCalls());
        }

        return serviceBuilder;

    }

    public ManagedServiceBuilder setEndpointServerBuilder(EndpointServerBuilder endpointServerBuilder) {
        this.endpointServerBuilder = endpointServerBuilder;
        return this;
    }



    public HealthServiceAsync getHealthService() {

        if (healthServiceBuilder == null) {
            if (healthService == null) {
                HealthServiceBuilder builder = getHealthServiceBuilder();
                healthService = builder.setAutoFlush().buildAndStart();
            }

            return healthService;
        } else {
            return healthServiceBuilder.buildHealthSystemReporter();
        }
    }

    public ManagedServiceBuilder setHealthService(HealthServiceAsync healthServiceAsync) {
        this.healthService = healthServiceAsync;
        return this;
    }

    public boolean isEnableStats() {
        return enableStats;
    }

    public ManagedServiceBuilder setEnableStats(boolean enableStats) {
        this.enableStats = enableStats;
        return this;
    }


    public int getSampleStatFlushRate() {
        return sampleStatFlushRate;
    }

    public ManagedServiceBuilder setSampleStatFlushRate(int sampleStatFlushRate) {
        this.sampleStatFlushRate = sampleStatFlushRate;
        return this;
    }

    public int getCheckTimingEveryXCalls() {
        return checkTimingEveryXCalls;
    }

    public ManagedServiceBuilder setCheckTimingEveryXCalls(int checkTimingEveryXCalls) {
        this.checkTimingEveryXCalls = checkTimingEveryXCalls;
        return this;
    }
}
