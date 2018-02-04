package com.serverless.service

import com.amazonaws.services.lambda.runtime.events.S3Event
import com.amazonaws.services.s3.model.ObjectMetadata
import com.amazonaws.services.s3.model.S3Object
import com.itextpdf.kernel.geom.PageSize
import com.itextpdf.kernel.pdf.PdfDocument
import com.itextpdf.kernel.pdf.PdfReader
import com.itextpdf.kernel.pdf.PdfWriter
import com.itextpdf.layout.Document
import com.itextpdf.layout.element.AreaBreak
import com.itextpdf.layout.element.Paragraph
import com.itextpdf.layout.property.AreaBreakType
import org.apache.log4j.BasicConfigurator
import org.apache.log4j.Logger
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.InputStream
import java.util.Date

/**
 * @author: Anirudh Karanam
 **/
class PdfService {
    private val s3Service: AmazonService

    init {
        BasicConfigurator.configure()
        s3Service = AmazonService()
    }

    fun processRequest(s3Event: S3Event) {
        LOG.info("PDF: processing the request")
        val s3Object = s3Service.getS3Object(s3Event)
        val bucketName = "pdfkit-target"
        transferObject(s3Object, bucketName)
    }

    private fun transferObject(s3Object: S3Object, bucketName: String) {
        val stream: InputStream = manipulateDocument(s3Object)
        val metaData = ObjectMetadata()
        metaData.lastModified = Date()
        metaData.addUserMetadata("Writer", "Anirudh")

        val objectKey = "${metaData.lastModified.time}_${s3Object.key}"
        LOG.debug("PDF: Copying to $bucketName as $objectKey")

        s3Service.transferObject(stream, metaData, bucketName, objectKey)
        LOG.debug("PDF: Transfer Complete!")
    }
    private fun manipulateDocument(s3Object: S3Object): InputStream {
        val outputStream = ByteArrayOutputStream()
        LOG.debug("PDF: Creating")
        val pdfDoc = PdfDocument(PdfReader(s3Object.objectContent), PdfWriter(outputStream))
        val doc = Document(pdfDoc)
        val areaBreak = AreaBreak(AreaBreakType.NEXT_PAGE)
        areaBreak.pageSize = PageSize(pdfDoc.lastPage.pageSize)
        LOG.debug("PDF: Adding PageBreak")
        doc.add(areaBreak)
        LOG.debug("PDF: Adding Some text to the new page")
        doc.add(Paragraph("Welcome to the World of Java/Kotlin"))
        pdfDoc.close()
        LOG.debug("PDF: Updated")
        return ByteArrayInputStream(outputStream.toByteArray())
    }

    companion object {
        private val LOG = Logger.getLogger(PdfService::class.java)
    }
}