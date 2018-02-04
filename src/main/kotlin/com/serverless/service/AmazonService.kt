package com.serverless.service

import com.amazonaws.services.s3.AmazonS3
import com.amazonaws.services.s3.AmazonS3ClientBuilder
import com.amazonaws.services.lambda.runtime.events.S3Event;
import com.amazonaws.services.s3.model.S3Object
import com.amazonaws.services.s3.model.ObjectMetadata
import com.amazonaws.services.s3.transfer.TransferManager
import com.amazonaws.services.s3.transfer.TransferManagerBuilder
import com.amazonaws.services.s3.transfer.Upload
import org.apache.log4j.BasicConfigurator
import org.apache.log4j.Logger
import java.io.InputStream

class AmazonService {
    private val s3Client: AmazonS3

    init {
        BasicConfigurator.configure()
        this.s3Client = AmazonS3ClientBuilder.standard().build()
    }

    fun getS3Object(event: S3Event): S3Object {
        val s3Entity = event.records[0].s3
        val bucket = s3Entity.bucket.name
        val key = s3Entity.`object`.key
        LOG.info("S3: Downloading the object")
        return s3Client.getObject(bucket, key)
    }

    fun transferObject(stream: InputStream, metaData: ObjectMetadata, bucket: String, key: String) {
        val transferManager:TransferManager = TransferManagerBuilder.standard().withS3Client(s3Client).build()
        LOG.debug("S3: Uploading to $bucket, with key: $key")
        val upload: Upload = transferManager.upload(bucket, key, stream, metaData)
        upload.waitForUploadResult()
        LOG.debug("Upload successful")
        stream.close()
        transferManager.shutdownNow(false)
    }

    companion object {
        private val LOG = Logger.getLogger(AmazonService::class.java)
    }
}