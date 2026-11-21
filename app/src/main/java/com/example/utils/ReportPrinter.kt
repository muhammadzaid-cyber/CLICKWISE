package com.example.utils

import android.content.Context
import android.graphics.Color
import android.graphics.Paint
import android.graphics.pdf.PdfDocument
import android.os.Bundle
import android.os.CancellationSignal
import android.os.ParcelFileDescriptor
import android.print.PageRange
import android.print.PrintAttributes
import android.print.PrintDocumentAdapter
import android.print.PrintDocumentInfo
import android.print.PrintManager
import android.print.pdf.PrintedPdfDocument
import android.text.Layout
import android.text.StaticLayout
import android.text.TextDirectionHeuristics
import android.text.TextPaint
import java.io.FileOutputStream

object ReportPrinter {

    fun printReport(context: Context, jobName: String, reportText: String): Boolean {
        val printManager = context.getSystemService(Context.PRINT_SERVICE) as? PrintManager ?: return false
        return try {
            val adapter = PlainTextPrintAdapter(context, jobName, reportText)
            printManager.print(jobName, adapter, PrintAttributes.Builder().build())
            true
        } catch (_: Exception) {
            false
        }
    }

    private class PlainTextPrintAdapter(
        private val context: Context,
        private val jobName: String,
        private val reportText: String
    ) : PrintDocumentAdapter() {

        private var pdfDocument: PrintedPdfDocument? = null
        private var pagesContent: List<List<String>> = emptyList()

        override fun onLayout(
            oldAttributes: PrintAttributes?,
            newAttributes: PrintAttributes,
            cancellationSignal: CancellationSignal?,
            callback: LayoutResultCallback,
            extras: Bundle?
        ) {
            pdfDocument = PrintedPdfDocument(context, newAttributes)
            if (cancellationSignal?.isCanceled == true) {
                callback.onLayoutCancelled()
                return
            }

            val pageWidthPt = ((newAttributes.mediaSize?.widthMils ?: 8500) * 72f / 1000f).toInt()
            val pageHeightPt = ((newAttributes.mediaSize?.heightMils ?: 11000) * 72f / 1000f).toInt()
            val usableWidth = (pageWidthPt - 80).coerceAtLeast(200)
            val usableHeight = (pageHeightPt - 96).coerceAtLeast(200)

            val paint = TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
                color = Color.BLACK
                textSize = 11f
            }

            val rawLines = reportText.lines()
            val wrappedPages = mutableListOf<List<String>>()
            var currentPage = mutableListOf<String>()
            var currentHeight = 0f

            for (line in rawLines) {
                val safeLine = if (line.isEmpty()) " " else line
                val staticLayout = StaticLayout.Builder
                    .obtain(safeLine, 0, safeLine.length, paint, usableWidth)
                    .setAlignment(Layout.Alignment.ALIGN_NORMAL)
                    .setTextDirection(TextDirectionHeuristics.FIRSTSTRONG_LTR)
                    .setLineSpacing(2f, 1.15f)
                    .build()

                val blockHeight = staticLayout.height.toFloat()
                if (currentHeight + blockHeight > usableHeight && currentPage.isNotEmpty()) {
                    wrappedPages.add(currentPage)
                    currentPage = mutableListOf()
                    currentHeight = 0f
                }
                currentPage.add(safeLine)
                currentHeight += blockHeight
            }
            if (currentPage.isNotEmpty()) {
                wrappedPages.add(currentPage)
            }
            if (wrappedPages.isEmpty()) {
                wrappedPages.add(listOf("CLICKWISE Security Report"))
            }
            pagesContent = wrappedPages

            val info = PrintDocumentInfo.Builder("$jobName.pdf")
                .setContentType(PrintDocumentInfo.CONTENT_TYPE_DOCUMENT)
                .setPageCount(pagesContent.size)
                .build()
            callback.onLayoutFinished(info, true)
        }

        override fun onWrite(
            pages: Array<out PageRange>,
            destination: ParcelFileDescriptor,
            cancellationSignal: CancellationSignal?,
            callback: WriteResultCallback
        ) {
            val doc = pdfDocument ?: run {
                callback.onWriteFailed("PDF document not initialized")
                return
            }

            val paint = TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
                color = Color.BLACK
                textSize = 11f
            }

            try {
                pagesContent.forEachIndexed { pageIndex, lines ->
                    if (cancellationSignal?.isCanceled == true) {
                        callback.onWriteCancelled()
                        doc.close()
                        pdfDocument = null
                        return
                    }
                    val page = doc.startPage(pageIndex)
                    val canvas = page.canvas
                    val usableWidth = (page.info.pageWidth - 80).coerceAtLeast(200)
                    var y = 48f
                    for (line in lines) {
                        val layout = StaticLayout.Builder
                            .obtain(line, 0, line.length, paint, usableWidth)
                            .setAlignment(Layout.Alignment.ALIGN_NORMAL)
                            .setTextDirection(TextDirectionHeuristics.FIRSTSTRONG_LTR)
                            .setLineSpacing(2f, 1.15f)
                            .build()
                        canvas.save()
                        canvas.translate(40f, y)
                        layout.draw(canvas)
                        canvas.restore()
                        y += layout.height
                    }
                    doc.finishPage(page)
                }

                FileOutputStream(destination.fileDescriptor).use { out ->
                    doc.writeTo(out)
                }
                callback.onWriteFinished(arrayOf(PageRange.ALL_PAGES))
            } catch (e: Exception) {
                callback.onWriteFailed(e.localizedMessage ?: "Failed to write print PDF")
            } finally {
                doc.close()
                pdfDocument = null
            }
        }

        override fun onFinish() {
            pdfDocument?.close()
            pdfDocument = null
            super.onFinish()
        }
    }
}
