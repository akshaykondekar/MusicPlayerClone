package com.example.musicplayerclone.ui.viewmodels

import android.support.v4.media.MediaBrowserCompat
import android.support.v4.media.MediaMetadataCompat.METADATA_KEY_MEDIA_ID
import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.musicplayerclone.data.entities.Song
import com.example.musicplayerclone.exoplayer.isPlayEnable
import com.example.musicplayerclone.exoplayer.isPlaying
import com.example.musicplayerclone.exoplayer.isPrepared
import com.example.musicplayerclone.other.Constants.MEDIA_ROOT_ID
import com.example.musicplayerclone.other.MusicServiceConnection
import com.example.musicplayerclone.other.Resource

class MainViewModel @ViewModelInject constructor(
    private val musicServiceConnection: MusicServiceConnection
) : ViewModel() {
    private val _mediaItems = MutableLiveData<Resource<List<Song>>>()
    val mediaItems: LiveData<Resource<List<Song>>> = _mediaItems

    val isConnected = musicServiceConnection.isConnected
    val networkError = musicServiceConnection.networkError
    val currentlyPlayingSong = musicServiceConnection.currentlyPlayingSong
    val playbackState = musicServiceConnection.playbackState

    init {

        _mediaItems.postValue(Resource.loading(null))
        musicServiceConnection.subscribe(MEDIA_ROOT_ID, object : MediaBrowserCompat.SubscriptionCallback(){
            override fun onChildrenLoaded(
                parentId: String,
                children: MutableList<MediaBrowserCompat.MediaItem>
            ) {
                super.onChildrenLoaded(parentId, children)
                val items = children.map{
                    Song(
                        it.mediaId!!,
                        it.description.title.toString(),
                        it.description.subtitle.toString(),
                        it.description.mediaUri.toString(),
                        it.description.iconUri.toString()
                    )
                }
                _mediaItems.postValue(Resource.success(items))
            }
        })
    }

    fun skipToNextSong(){
        musicServiceConnection.transportControls.skipToNext()
    }

    fun skipToPreviousSong(){
        musicServiceConnection.transportControls.skipToPrevious()
    }

    fun seekTo(pos : Long){
        musicServiceConnection.transportControls.seekTo(pos)
    }

    fun playOrToggleSong(mediaItem : Song, toggle : Boolean = false){
        val isPrepared = playbackState.value?.isPrepared ?: false

        if(isPrepared && mediaItem.mediaId==
            currentlyPlayingSong.value?.getString(METADATA_KEY_MEDIA_ID)){
            playbackState.value?.let {playbackState ->
                when{
                    playbackState.isPlaying -> if(toggle) musicServiceConnection.transportControls.pause()
                    playbackState.isPlayEnable -> musicServiceConnection.transportControls.play()
                    else -> Unit
                }
            }
        }
        else{
            musicServiceConnection.transportControls.playFromMediaId(mediaItem.mediaId, null)
        }
    }

    override fun onCleared() {
        super.onCleared()
        musicServiceConnection.unSubscribe(MEDIA_ROOT_ID, object : MediaBrowserCompat.SubscriptionCallback(){})
    }
}