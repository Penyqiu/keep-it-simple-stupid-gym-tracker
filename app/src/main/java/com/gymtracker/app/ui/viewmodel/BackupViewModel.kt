package com.gymtracker.app.ui.viewmodel

import android.content.Context
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gymtracker.app.data.repository.BackupRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class BackupViewModel @Inject constructor(
    private val backupRepository: BackupRepository
) : ViewModel() {

    fun exportBackup(context: Context, uri: Uri, onResult: (Boolean) -> Unit) =
        viewModelScope.launch { onResult(backupRepository.exportToJson(context, uri)) }

    fun importBackup(context: Context, uri: Uri, onResult: (Boolean) -> Unit) =
        viewModelScope.launch { onResult(backupRepository.importFromJson(context, uri)) }
}
