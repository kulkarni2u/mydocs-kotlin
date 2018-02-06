package com.serverless.handler

import com.amazonaws.services.lambda.runtime.Context
import com.amazonaws.services.lambda.runtime.RequestHandler
import com.amazonaws.services.lambda.runtime.events.S3Event
import com.serverless.ApiGatewayResponse
import com.serverless.service.PdfService
import org.apache.log4j.BasicConfigurator
import org.apache.log4j.Logger
import java.util.Collections

class AddPageHandler :RequestHandler<S3Event, ApiGatewayResponse> {
    private val pdfService: PdfService = PdfService()

    override fun handleRequest(input:S3Event, context:Context): ApiGatewayResponse {
        BasicConfigurator.configure()
        LOG.info("PDF Lambda Execution Started")
        pdfService.processRequest(input)
        LOG.info("Completed the process")
        return ApiGatewayResponse.build {
            statusCode = 200
            rawBody = "Go Serverless v1.x! Your Kotlin function executed successfully!"
            headers = Collections.singletonMap<String, String>("X-Powered-By", "AWS Lambda & serverless")
        }
    }
    companion object {
        private val LOG = Logger.getLogger(AddPageHandler::class.java)
    }
}