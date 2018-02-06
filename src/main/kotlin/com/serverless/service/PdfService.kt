package com.serverless.service

import com.amazonaws.services.lambda.runtime.events.S3Event
import com.amazonaws.services.s3.model.ObjectMetadata
import com.amazonaws.services.s3.model.S3Object
import com.fasterxml.jackson.databind.ObjectMapper
import com.itextpdf.kernel.geom.PageSize
import com.itextpdf.kernel.pdf.PdfDocument
import com.itextpdf.kernel.pdf.PdfReader
import com.itextpdf.kernel.pdf.PdfWriter
import com.itextpdf.layout.Document
import com.itextpdf.layout.element.AreaBreak
import com.itextpdf.layout.element.Paragraph
import com.itextpdf.layout.property.AreaBreakType
import com.serverless.model.RequestMapper
import org.apache.log4j.BasicConfigurator
import org.apache.log4j.Logger
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.InputStream
import java.util.*

/**
 * @author: Anirudh Karanam
 **/
class PdfService {
    private val s3Service: AmazonService
    private val targetBucket = "pdfkit-target"
    private val MESSAGE = "Welcome to the World of Java/Kotlin"

    init {
        BasicConfigurator.configure()
        s3Service = AmazonService()
    }

    fun processRequest(s3Event: S3Event) {
        LOG.info("PDF: processing the request")
        val s3Object = s3Service.getS3Object(s3Event)
        transferObject(s3Object)
    }

    private fun readS3Object(s3Object: S3Object): InputStream {
        s3Object.objectContent
        val mapper = ObjectMapper()
        val (content, file) = mapper.readValue<RequestMapper>(s3Object.objectContent, RequestMapper::class.java)
        val decode: ByteArray = Base64.getDecoder().decode(file)
        val inputStream: InputStream = ByteArrayInputStream(decode)
        return manipulateDocument(inputStream, content)
    }

    private fun transferObject(s3Object: S3Object, bucketName: String = targetBucket) {
        val stream: InputStream = readS3Object(s3Object)
        val metaData = ObjectMetadata()
        metaData.lastModified = Date()
        metaData.addUserMetadata("Writer", "Anirudh")

        val objectKey = "${metaData.lastModified.time}_${s3Object.key}"
        LOG.debug("PDF: Copying to $bucketName as $objectKey")

        s3Service.transferObject(stream, metaData, bucketName, objectKey)
        LOG.debug("PDF: Transfer Complete!")
    }
    private fun manipulateDocument(inputStream: InputStream, content: String = MESSAGE): InputStream {
        val outputStream = ByteArrayOutputStream()
        LOG.debug("PDF: Creating")
        val pdfDoc = PdfDocument(PdfReader(inputStream), PdfWriter(outputStream))
        val doc = Document(pdfDoc)
        val areaBreak = AreaBreak(AreaBreakType.NEXT_PAGE)
        areaBreak.pageSize = PageSize(pdfDoc.lastPage.pageSize)
        LOG.debug("PDF: Adding PageBreak")
        doc.add(areaBreak)
        LOG.debug("PDF: Adding Some text to the new page")
        doc.add(Paragraph(content))
        pdfDoc.close()
        LOG.debug("PDF: Updated")
        return ByteArrayInputStream(outputStream.toByteArray())
    }

    companion object {
        private val LOG = Logger.getLogger(PdfService::class.java)
    }
}