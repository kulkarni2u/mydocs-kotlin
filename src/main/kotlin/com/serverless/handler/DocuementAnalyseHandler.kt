package com.serverless.handler

import com.amazonaws.services.lambda.runtime.Context
import com.amazonaws.services.lambda.runtime.RequestHandler
import com.amazonaws.services.lambda.runtime.events.S3Event
import com.serverless.ApiGatewayResponse
import com.serverless.service.DocumentAnalyser
import org.apache.log4j.BasicConfigurator
import org.apache.log4j.Logger
import java.util.Collections

class DocuementAnalyseHandler : RequestHandler<S3Event, ApiGatewayResponse> {
    private val documentAnalyser = DocumentAnalyser()
    override fun handleRequest(s3Event: S3Event, context: Context): ApiGatewayResponse {
        BasicConfigurator.configure()
        LOG.info("JSON Based Lambda Execution started")
        documentAnalyser.processRequest(s3Event)
        LOG.info("Process completed successfully")
        return ApiGatewayResponse.build {
            statusCode = 200
            rawBody = "Go Serverless v1.x! Your Kotlin function executed successfully!"
            headers = Collections.singletonMap<String, String>("X-Powered-By", "AWS Lambda & serverless")
        }
    }

    companion object {
        private val LOG = Logger.getLogger(DocuementAnalyseHandler::class.java)
    }
}