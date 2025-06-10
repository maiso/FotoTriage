package com.maiso.fototriage

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.Month
import java.time.Year

data class OverviewScreenUiState(
    val yearPhotos: List<Pair<Year, Int>> = emptyList(),
    val monthPhotos: List<MonthUiState> = emptyList(),
)

data class MonthUiState(
    val year: Year,
    val month: Month,
    val nrOfPhoto: Int,
    val loading: Boolean = false,
)

class OverviewScreenViewModel : ViewModel() {

    val years = FotoDatabase.photos.findUniqueYears()

    val months = listOf(
        "Januari", "Februari", "Maart", "April", "Mei", "Juni",
        "Juli", "Augustus", "September", "Oktober", "November", "December"
    )

    val uiState = MutableStateFlow<OverviewScreenUiState>(
        OverviewScreenUiState()
    )

    init {

        years.forEach { year ->
            val photosOfTheYear = FotoDatabase.photos.filterByYear(year)
            val nrOfPhotosPerYear = photosOfTheYear.size

            uiState.update {
                it.copy(
                    yearPhotos = it.yearPhotos.toMutableList().apply {
                        add(Pair(year, nrOfPhotosPerYear))
                    }
                )
            }

            Month.entries.forEach { month ->
                val photosPerMonth = photosOfTheYear.filterByMonth(year, month)
                val nrOfPhotosPerMonth = photosPerMonth.size
                var loading = true
                viewModelScope.launch {
                    FotoDatabase.hashFilesInParallel(photosPerMonth.map { it.filePath })
                    Log.i("MVDB", "Finished hashing $month")
                    loading = false
                    uiState.update { overviewScreenUiState ->
                        overviewScreenUiState.copy(monthPhotos = overviewScreenUiState.monthPhotos.map {
                            if (it.month == month) {
                                it.copy(loading = false)
                            } else {
                                it
                            }
                        }
                        )
                    }
                }
                uiState.update {
                    it.copy(
                        monthPhotos = it.monthPhotos.toMutableList().apply {
                            add(
                                MonthUiState(
                                    year,
                                    month,
                                    nrOfPhotosPerMonth,
                                    loading
                                )
                            )
                        }
                    )
                }
            }
        }
    }

    companion object {
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                OverviewScreenViewModel()
            }
        }
    }

}