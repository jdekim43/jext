package kr.jadekim.jext.koin

import kr.jadekim.jext.koin.util.loadPropertiesFromArguments
import kr.jadekim.jext.koin.util.shutdownHook
import kr.jadekim.common.enumeration.Environment
import kr.jadekim.common.enumeration.IEnvironment
import kr.jadekim.common.util.parseArgument
import kr.jadekim.logger.JLog
import kr.jadekim.logger.JLogger
import kr.jadekim.logger.context.GlobalLogContext
import kr.jadekim.logger.integration.koin.KoinLogger
import kr.jadekim.logger.pipeline.LoggerNameShorter
import kr.jadekim.logger.pipeline.StdOutPrinter
import org.koin.core.context.KoinContext
import org.koin.core.module.Module
import org.koin.core.qualifier.StringQualifier
import org.koin.dsl.bind
import org.koin.dsl.module
import org.koin.mp.KoinPlatformTools

abstract class BaseKoinApplication(
    private vararg val args: String,
    applicationName: String? = null,
    val environment: IEnvironment? = null,
    val version: String? = null,
    logger: JLogger? = null,
    properties: Map<String, String>? = null,
    val koinContext: KoinContext = KoinPlatformTools.defaultContext(),
) {

    open class EnvironmentOption {
        var enableLoggerNameShorter: Boolean = true
    }

    abstract val modules: List<Module>

    val applicationName: String = applicationName ?: this::class.simpleName ?: "KoinApplication"
    val propertiesBeanQualifier = StringQualifier("${this.applicationName}-properties")

    var isInitialized = false
        private set

    var properties: Map<String, String> = properties ?: emptyMap()
        protected set

    lateinit var environmentOption: EnvironmentOption
        protected set

    val isGlobal: Boolean = koinContext == KoinPlatformTools.defaultContext()

    protected val logger = logger ?: JLog.get(this.applicationName)

    private var isStart = false

    protected abstract fun startApplication()

    protected abstract fun stopApplication()

    fun init(arguments: Map<String, List<String>> = parseArgument(*args)) {
        logger.info {
            meta = arguments

            "Init $applicationName with ${arguments.size} arguments"
        }

        environmentOption = getEnvironmentOption(environment)
        properties = loadPropertiesFromArguments(arguments) + properties

        if (isGlobal) {
            GlobalLogContext["applicationName"] = this.applicationName
            GlobalLogContext["environment"] = environment?.name
            GlobalLogContext["version"] = version

            configureJLogger()
        }

        onInit()
        isInitialized = true
    }

    fun start() {
        if (!isInitialized) {
            init()
        }

        logger.info { "Start $applicationName" }

        initContainer()
        onInitializedContainer()

        shutdownHook {
            if (isStart) {
                stop()
            }
        }

        isStart = true
        startApplication()
    }

    fun stop() {
        isStart = false
        stopApplication()
        koinContext.stopKoin()
    }

    protected open fun configureJLogger() {
        JLog.pipeline = mutableListOf()

        if (environmentOption.enableLoggerNameShorter) {
            JLog.installPipe(LoggerNameShorter())
        }

        JLog.installPipe(StdOutPrinter())
    }

    protected open fun onInit() {
        //do nothing
    }

    protected open fun initContainer() = koinContext.startKoin {
        logger(KoinLogger())
        properties(properties)
        modules(module {
            single { environment } bind Environment::class
            single(propertiesBeanQualifier) { properties }
            single { properties }
        })
        modules(this@BaseKoinApplication.modules)
    }

    protected open fun onInitializedContainer() {
        //do nothing
    }

    protected open fun getEnvironmentOption(environment: IEnvironment?): EnvironmentOption {
        val env = environment as? Environment ?: return EnvironmentOption()

        return when (env) {
            Environment.LOCAL -> EnvironmentOption().apply {
                enableLoggerNameShorter = false
            }
            else -> EnvironmentOption().apply {
                enableLoggerNameShorter = true
            }
        }
    }

    private operator fun <T> List<T>?.plus(data: List<T>?): List<T> {
        val result = mutableListOf<T>()

        this?.also { result.addAll(it) }
        data?.also { result.addAll(it) }

        return result
    }
}