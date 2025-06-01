package com.maiso.fototriage

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update
import java.time.Month
import java.time.Year

data class OverviewScreenUiState(
    val yearPhotos: List<Pair<Year, Int>> = emptyList(),
    val monthPhotos: List<Triple<Year, Month, Int>> = emptyList(),
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

                uiState.update {
                    it.copy(
                        monthPhotos = it.monthPhotos.toMutableList().apply {
                            add(Triple(year, month, nrOfPhotosPerMonth))
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