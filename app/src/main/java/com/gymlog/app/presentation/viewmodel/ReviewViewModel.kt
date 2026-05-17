package com.gymlog.app.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gymlog.app.domain.model.*
import com.gymlog.app.domain.repository.*
import com.gymlog.app.domain.usecase.ReviewUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ReviewViewModel @Inject constructor(
    private val reviewRepo: ReviewRepository,
    private val reviewUseCase: ReviewUseCase
) : ViewModel() {

    private val _review = MutableStateFlow<ReviewRequest?>(null)
    val review: StateFlow<ReviewRequest?> = _review

    private val _loading = MutableStateFlow(false)
    val loading: StateFlow<Boolean> = _loading

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error

    fun loadReview(sessionId: String) {
        viewModelScope.launch {
            _loading.value = true
            try {
                _review.value = reviewRepo.getBySessionId(sessionId)
            } catch (e: Exception) {
                _error.value = e.message
            } finally {
                _loading.value = false
            }
        }
    }

    fun submitForReview(sessionId: String) {
        viewModelScope.launch {
            _loading.value = true
            _error.value = null
            try {
                reviewUseCase.submitReview(sessionId)
                _review.value = reviewRepo.getBySessionId(sessionId)
            } catch (e: Exception) {
                _error.value = e.message
            } finally {
                _loading.value = false
            }
        }
    }

    fun executeReview(reviewId: String) {
        viewModelScope.launch {
            _loading.value = true
            try {
                reviewUseCase.executeReview(reviewId)
                _review.value = reviewRepo.getById(reviewId)
            } catch (e: Exception) {
                _error.value = e.message
            } finally {
                _loading.value = false
            }
        }
    }

    fun resolveItem(itemId: String) {
        viewModelScope.launch {
            val r = _review.value ?: return@launch
            val updatedItems = r.items.map {
                if (it.id == itemId) it.copy(userAction = UserAction.RESOLVED) else it
            }
            val updated = r.copy(items = updatedItems)
            _review.value = updated
            reviewRepo.update(updated)
        }
    }

    fun dismissItem(itemId: String) {
        viewModelScope.launch {
            val r = _review.value ?: return@launch
            val updatedItems = r.items.map {
                if (it.id == itemId) it.copy(userAction = UserAction.DISMISSED) else it
            }
            val updated = r.copy(items = updatedItems)
            _review.value = updated
            reviewRepo.update(updated)
        }
    }

    fun clearError() { _error.value = null }
}
