package com.example.musicplayerclone.ui.viewmodels

import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.musicplayerclone.exoplayer.MusicService
import com.example.musicplayerclone.exoplayer.currentPlaybackPosition
import com.example.musicplayerclone.other.Constants.UPDATE_PLAYER_POSITION_INTERVAL
import com.example.musicplayerclone.other.MusicServiceConnection
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class SongViewModel @ViewModelInject constructor(
    musicServiceConnection: MusicServiceConnection
) : ViewModel(){
    private val playbackState = musicServiceConnection.playbackState

    private val _currentSongDuration = MutableLiveData<Long>()
    val currentSongDuration : LiveData<Long> = _currentSongDuration

    private val _currentPlayerPosition = MutableLiveData<Long>()
    val currentPlayerPosition : LiveData<Long> = _currentPlayerPosition

    init {
        updateCurrentPlayerPosition()
    }

    private fun updateCurrentPlayerPosition(){
        viewModelScope.launch {
            while (true){
                val pos = playbackState.value?.currentPlaybackPosition
                if(currentPlayerPosition.value != pos){
                    _currentPlayerPosition.postValue(pos)
                    _currentSongDuration.postValue(MusicService.currentSongDuration)
                }
                delay(UPDATE_PLAYER_POSITION_INTERVAL)
            }
        }
    }
}