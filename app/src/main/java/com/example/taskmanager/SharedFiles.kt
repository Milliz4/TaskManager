package com.example.taskmanager

import android.content.ContentValues
import android.content.Context
import android.media.MediaScannerConnection
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import androidx.annotation.RequiresApi
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.InputStream
import java.io.OutputStream

/**
 * Общее хранилище — папка Download.
 *  Android 10+ (API 29+): MediaStore, разрешения не нужны (scoped storage).
 *  Android 8–9 (API < 29): обычные файлы + разрешение WRITE_EXTERNAL_STORAGE.
 */
object SharedFiles {

    fun write(ctx: Context, name: String, mime: String, block: (OutputStream) -> Unit): Boolean =
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) Modern.write(ctx, name, mime, block)
        else Legacy.write(ctx, name, block)

    fun read(ctx: Context, name: String, block: (InputStream) -> Unit): Boolean =
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) Modern.read(ctx, name, block)
        else Legacy.read(ctx, name, block)

    fun delete(ctx: Context, name: String): Boolean =
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) Modern.delete(ctx, name)
        else Legacy.delete(ctx, name)

    // ================= Android 10+ =================
    @RequiresApi(Build.VERSION_CODES.Q)
    private object Modern {

        private val collection = MediaStore.Downloads.EXTERNAL_CONTENT_URI

        private fun find(ctx: Context, name: String): Uri? {
            ctx.contentResolver.query(
                collection,
                arrayOf(MediaStore.Downloads._ID),
                "${MediaStore.Downloads.DISPLAY_NAME} = ?",
                arrayOf(name),
                null
            )?.use { c ->
                if (c.moveToFirst()) {
                    return Uri.withAppendedPath(collection, c.getLong(0).toString())
                }
            }
            return null
        }

        private fun create(ctx: Context, name: String, mime: String): Uri? {
            val values = ContentValues().apply {
                put(MediaStore.Downloads.DISPLAY_NAME, name)
                put(MediaStore.Downloads.MIME_TYPE, mime)
                put(MediaStore.Downloads.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS)
            }
            return ctx.contentResolver.insert(collection, values)
        }

        fun write(ctx: Context, name: String, mime: String, block: (OutputStream) -> Unit): Boolean =
            try {
                val uri = find(ctx, name) ?: create(ctx, name, mime) ?: return false
                ctx.contentResolver.openOutputStream(uri, "wt")?.use(block) != null
            } catch (e: Exception) {
                false
            }

        fun read(ctx: Context, name: String, block: (InputStream) -> Unit): Boolean =
            try {
                val uri = find(ctx, name) ?: return false
                ctx.contentResolver.openInputStream(uri)?.use(block) != null
            } catch (e: Exception) {
                false
            }

        fun delete(ctx: Context, name: String): Boolean {
            val uri = find(ctx, name) ?: return false
            return ctx.contentResolver.delete(uri, null, null) > 0
        }
    }

    // ================= Android 8–9 (API 24–28) =================
    private object Legacy {

        private fun dir(): File =
            Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)

        fun write(ctx: Context, name: String, block: (OutputStream) -> Unit): Boolean =
            try {
                val d = dir()
                if (!d.exists()) d.mkdirs()
                val file = File(d, name)
                FileOutputStream(file).use(block)          // перезапись с нуля
                MediaScannerConnection.scanFile(ctx, arrayOf(file.absolutePath), null, null)
                true
            } catch (e: Exception) {
                false
            }

        fun read(ctx: Context, name: String, block: (InputStream) -> Unit): Boolean {
            return try {
                val file = File(dir(), name)
                if (!file.exists()) return false
                FileInputStream(file).use { block(it) }
                true
            } catch (e: Exception) {
                false
            }
        }

        fun delete(ctx: Context, name: String): Boolean =
            try {
                val file = File(dir(), name)
                val ok = file.delete()
                if (ok) MediaScannerConnection.scanFile(ctx, arrayOf(file.absolutePath), null, null)
                ok
            } catch (e: Exception) {
                false
            }
    }
}