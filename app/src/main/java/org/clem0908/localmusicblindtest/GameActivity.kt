package org.clem0908.localmusicblindtest

import android.graphics.BitmapFactory
import android.media.MediaMetadataRetriever
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.os.SystemClock
import android.provider.DocumentsContract
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.documentfile.provider.DocumentFile
import androidx.lifecycle.lifecycleScope
import androidx.media3.common.MediaItem
import androidx.media3.exoplayer.ExoPlayer
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlin.random.Random
import kotlin.math.min

class GameActivity : AppCompatActivity() {

    private lateinit var repository: FolderRepository
    private var audioFiles: MutableList<Uri> = mutableListOf()
    private var playedIndices: MutableSet<Int> = mutableSetOf()
    private var currentIndex: Int = -1
    private var startPositionMs: Long = 0
    private var durationMs: Long = 30_000L

    private lateinit var player: ExoPlayer
    private lateinit var ivCover: ImageView
    private lateinit var tvTitle: TextView
    private lateinit var tvAlbum: TextView
    private lateinit var tvYear: TextView
    private lateinit var tvTime: TextView
    private lateinit var tvArtist: TextView
    private lateinit var btnStart: Button
    private lateinit var btnStop: Button
    private lateinit var btnRestart: Button
    private lateinit var btnReveal: Button
    private lateinit var btnNext: Button

    private var randomStart: Boolean = false
    private val handler = Handler(Looper.getMainLooper())

    private lateinit var progressBar: ProgressBar

    private var startTime: Long? = null
    private var elapsedTime: Long? = null

    private fun resetRevealUI() {
        ivCover.setImageResource(R.drawable.ic_music_placeholder)
        tvTitle = findViewById(R.id.tv_title)
        tvYear = findViewById(R.id.tv_year)
        tvTime = findViewById(R.id.tv_time)
        tvAlbum = findViewById(R.id.tv_album)
        tvArtist = findViewById(R.id.tv_artist)
        tvTitle.text = "\nTrack: ?"
        tvAlbum.text = "\nAlbum: ?"
        tvArtist.text = "\nArtist: ?"
        tvYear.text = "\nYear: ?"
        tvTime.text = "\nTime: N/A"
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_game)
        progressBar = findViewById(R.id.progress_scan)

        repository = FolderRepository(this)

        ivCover = findViewById(R.id.iv_cover)
        btnStart = findViewById(R.id.btn_start)
        btnStop = findViewById(R.id.btn_stop)
        btnRestart = findViewById(R.id.btn_restart)
        btnReveal = findViewById(R.id.btn_reveal)
        btnNext = findViewById(R.id.btn_next)

        randomStart = intent.getBooleanExtra("randomStart", false)
        durationMs = (intent.getIntExtra("duration", 30) * 1000).toLong()

        player = ExoPlayer.Builder(this).build()

        btnStart.isEnabled = false
        btnStop.isEnabled = false
        btnRestart.isEnabled = false
        btnReveal.isEnabled = false
        btnNext.isEnabled = false

        progressBar.visibility = View.VISIBLE

        lifecycleScope.launch {
            audioFiles = scanFolders().toMutableList()

            progressBar.visibility = View.GONE

            if (audioFiles.isEmpty()) {
                Toast.makeText(this@GameActivity, R.string.no_audio_files, Toast.LENGTH_LONG).show()
                finish()
                return@launch
            }
            selectNextSong()

            btnStart.isEnabled = true
            btnStop.isEnabled = true
            btnRestart.isEnabled = true
            btnReveal.isEnabled = true
            btnNext.isEnabled = true
        }

        btnStart.setOnClickListener { startSong() }
        btnStop.setOnClickListener { stopSong() }
        btnRestart.setOnClickListener { restartSong() }
        btnReveal.setOnClickListener { revealSong() }
        btnNext.setOnClickListener { nextSong() }
    }

    private suspend fun scanFolders(): List<Uri> = withContext(Dispatchers.IO) {
        val audioUris = mutableListOf<Uri>()
        val folders = repository.getAll()

        val allowedExtensions = listOf(
            "flac", "mp3", "aac", "opus", "ogg", "m4a", "wav", "alac", "aiff"
        )

        for (folder in folders) {
            val docFile = DocumentFile.fromTreeUri(this@GameActivity, Uri.parse(folder.uri))
            docFile?.listFiles()?.forEach { file ->
                if (file.isFile && allowedExtensions.any { file.name?.endsWith(it, true) == true }) {
                    audioUris.add(file.uri)
                }
            }
        }
        audioUris
    }

    private fun selectNextSong() {
        if (playedIndices.size >= audioFiles.size) {
            Toast.makeText(this, R.string.playlist_finished, Toast.LENGTH_LONG).show()
            finish()
            return
        }
        do {
            currentIndex = Random.nextInt(audioFiles.size)
        } while (playedIndices.contains(currentIndex))
        playedIndices.add(currentIndex)

        resetRevealUI()
        ivCover.setImageBitmap(null)
    }

    private fun startSong() {
        if (currentIndex == -1) return
        player.stop()
        resetRevealUI()
        startTime = SystemClock.elapsedRealtime()
        elapsedTime = null
        val uri = audioFiles[currentIndex]

        val mmr = MediaMetadataRetriever()
        mmr.setDataSource(this, uri)
        val trackDuration = mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)?.toLong() ?: 0L
        mmr.release()

        startPositionMs = if (randomStart && trackDuration > 0) Random.nextLong(0, trackDuration) else 0L
        val effectiveDuration = if (trackDuration > 0) min(durationMs, trackDuration - startPositionMs) else durationMs

        val mediaItem = MediaItem.fromUri(uri)
        player.setMediaItem(mediaItem)
        player.prepare()
        player.seekTo(startPositionMs)
        player.play()

        handler.postDelayed({ player.pause() }, effectiveDuration)
    }

    private fun stopSong() { player.stop() }

    private fun restartSong() { startSong() }

    private fun formatElapsedTime(ms: Long): String {
        val seconds = ms / 1000
        val millis = ms % 1000
        return "${seconds}s ${millis / 100}ms"
    }

    private fun revealSong() {
        if (currentIndex == -1) return

        if (startTime != null && elapsedTime == null) {
            elapsedTime = SystemClock.elapsedRealtime() - startTime!!
        }

        val timeText = if (elapsedTime != null) {
            "\nTime: ${formatElapsedTime(elapsedTime!!)}"
        } else {
            "\nTime: N/A"
        }

        val uri = audioFiles[currentIndex]

        var title: String?
        var album: String?
        var year: String?
        var artist: String?

        val mmr = MediaMetadataRetriever()
        try {
            mmr.setDataSource(this, uri)

            title = mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_TITLE)
            album = mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ALBUM)
            year = mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_YEAR)
                ?: mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DATE)
            artist = mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ARTIST)

            if (title.isNullOrBlank()) {
                val cursor = contentResolver.query(
                    uri,
                    arrayOf(DocumentsContract.Document.COLUMN_DISPLAY_NAME),
                    null, null, null
                )
                title = cursor?.use {
                    if (it.moveToFirst()) it.getString(0) else R.string.unknown.toString()
                } ?: R.string.unknown.toString()
            }

            tvTitle.text = "\nTrack: $title"
            tvAlbum.text = "\nAlbum: $album"
            tvTime.text = "$timeText"
            tvYear.text = "\nYear: $year"
            tvArtist.text = "\nArtist: $artist"

            // Cover
            val art = mmr.embeddedPicture
            if (art != null) {
                val bitmap = BitmapFactory.decodeByteArray(art, 0, art.size)
                ivCover.setImageBitmap(bitmap)
            } else {
                ivCover.setImageResource(R.drawable.ic_music_placeholder)
            }

        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            mmr.release()
        }
    }

    private fun nextSong() {
        startTime = null
        elapsedTime = null
        player.stop()
        selectNextSong()
    }

    override fun onDestroy() {
        super.onDestroy()
        player.release()
        handler.removeCallbacksAndMessages(null)
    }
}
