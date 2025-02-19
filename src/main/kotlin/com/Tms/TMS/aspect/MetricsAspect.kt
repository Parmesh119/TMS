package com.Tms.TMS.aspect

import com.Tms.TMS.util.MetricsUtil
import org.aspectj.lang.ProceedingJoinPoint
import org.aspectj.lang.annotation.Around
import org.aspectj.lang.annotation.Aspect
import org.springframework.stereotype.Component
import org.springframework.web.bind.annotation.RequestMapping
import java.util.concurrent.TimeUnit
import org.springframework.core.annotation.AnnotationUtils
import org.springframework.web.bind.annotation.*
import java.lang.reflect.Method

@Aspect
@Component
class MetricsAspect(private val metricsUtil: MetricsUtil) {

    @Around("@within(org.springframework.web.bind.annotation.RestController)")
    fun measureExecutionTime(joinPoint: ProceedingJoinPoint): Any {
        val startTime = System.nanoTime()
        var status = "success"
        val endpoint = getEndpointPath(joinPoint)

        try {
            // Register endpoint if not already registered
            metricsUtil.registerEndpoint(endpoint)
            metricsUtil.trackActiveRequests(endpoint, true)

            return joinPoint.proceed()
        } catch (e: Exception) {
            status = "error"
            throw e
        } finally {
            val timeTaken = TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - startTime)
            metricsUtil.recordApiMetrics(endpoint, status, timeTaken)
            metricsUtil.trackActiveRequests(endpoint, false)
        }
    }

    private fun getEndpointPath(joinPoint: ProceedingJoinPoint): String {
        val method = getMethod(joinPoint)
        val classRequestMapping = AnnotationUtils.findAnnotation(
            joinPoint.target.javaClass,
            RequestMapping::class.java
        )
        val basePath = classRequestMapping?.value?.firstOrNull() ?: ""

        val methodPath = when {
            method.isAnnotationPresent(GetMapping::class.java) ->
                method.getAnnotation(GetMapping::class.java).value.firstOrNull()
            method.isAnnotationPresent(PostMapping::class.java) ->
                method.getAnnotation(PostMapping::class.java).value.firstOrNull()
            method.isAnnotationPresent(PutMapping::class.java) ->
                method.getAnnotation(PutMapping::class.java).value.firstOrNull()
            method.isAnnotationPresent(DeleteMapping::class.java) ->
                method.getAnnotation(DeleteMapping::class.java).value.firstOrNull()
            method.isAnnotationPresent(RequestMapping::class.java) ->
                method.getAnnotation(RequestMapping::class.java).value.firstOrNull()
            else -> ""
        } ?: ""

        return "$basePath$methodPath"
    }

    private fun getMethod(joinPoint: ProceedingJoinPoint): Method {
        val signature = joinPoint.signature
        val methodName = signature.name
        val targetClass = joinPoint.target.javaClass
        return targetClass.methods.first { it.name == methodName }
    }
}