package com.example.musicplayerclone.ui.fragments

import android.os.Bundle
import android.support.v4.media.session.PlaybackStateCompat
import android.view.View
import android.widget.SeekBar
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.ViewModelProvider
import com.bumptech.glide.RequestManager
import com.example.musicplayerclone.R
import com.example.musicplayerclone.data.entities.Song
import com.example.musicplayerclone.exoplayer.isPlaying
import com.example.musicplayerclone.exoplayer.toSong
import com.example.musicplayerclone.other.Status
import com.example.musicplayerclone.ui.viewmodels.MainViewModel
import com.example.musicplayerclone.ui.viewmodels.SongViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.fragment_song.*
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject

@AndroidEntryPoint
class SongFragment : Fragment(R.layout.fragment_song) {

    @Inject
    lateinit var glide : RequestManager

    private lateinit var mainViewModel: MainViewModel
    private val songViewModel : SongViewModel by viewModels()

    private var currentPlayingSong : Song? = null

    private var playbackState : PlaybackStateCompat? = null

    private var shouldUpdateSeekbar = true

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        mainViewModel = ViewModelProvider(requireActivity()).get(MainViewModel::class.java)
        subscribeToObserver()

        seekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener{
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                if(fromUser){
                    setCurrentPlayerTimeToTextView(progress.toLong())
                }
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {
                shouldUpdateSeekbar = false
            }

            override fun onStopTrackingTouch(seekBar: SeekBar?) {
                seekBar?.let {
                    mainViewModel.seekTo(it.progress.toLong())
                    shouldUpdateSeekbar = true
                }
            }
        })

        ivPlayPauseDetail.setOnClickListener {
            currentPlayingSong?.let {
                mainViewModel.playOrToggleSong(it, true)
            }
        }

        ivSkipPrevious.setOnClickListener {
            mainViewModel.skipToPreviousSong()
        }

        ivSkip.setOnClickListener {
            mainViewModel.skipToNextSong()
        }
    }

    private fun updateTitleAndSongImage(song: Song){
        val title = "${song.title} - ${song.subtitle}"
        tvSongName.text = title
        glide.load(song.imageUrl).into(ivSongImage)
    }

    private fun subscribeToObserver(){
        mainViewModel.mediaItems.observe(viewLifecycleOwner){
            it?.let {result ->
                when(result.status){
                    Status.SUCCESS ->{
                        result.data?.let { songs ->
                            if(currentPlayingSong == null && songs.isNotEmpty()){
                                currentPlayingSong = songs[0]
                                updateTitleAndSongImage(songs[0])
                            }
                        }
                    }
                    else -> Unit
                }
            }
        }

        mainViewModel.currentlyPlayingSong.observe(viewLifecycleOwner){
            if(it == null) return@observe
            currentPlayingSong = it.toSong()
            updateTitleAndSongImage(currentPlayingSong!!)
        }

        mainViewModel.playbackState.observe(viewLifecycleOwner){
            playbackState = it
            ivPlayPauseDetail.setImageResource(
                if(playbackState?.isPlaying == true) R.drawable.ic_pause else R.drawable.ic_play
            )

            seekBar.progress = it?.position?.toInt() ?: 0
        }

        songViewModel.currentPlayerPosition.observe(viewLifecycleOwner){
            if(shouldUpdateSeekbar){
                seekBar.progress = it.toInt()
                setCurrentPlayerTimeToTextView(it)
            }
        }

        songViewModel.currentSongDuration.observe(viewLifecycleOwner){
            seekBar.max = it.toInt()
            val dateFormat = SimpleDateFormat("mm:ss", Locale.getDefault())
            tvSongDuration.text = dateFormat.format(it)
        }
    }

    private fun setCurrentPlayerTimeToTextView(ms: Long?) {
        val dateFormat = SimpleDateFormat("mm:ss", Locale.getDefault())
        tvCurTime.text = dateFormat.format(ms)
    }
}