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
    val yearPhotos: List<YearUiState> = emptyList(),
    val monthPhotos: List<MonthUiState> = emptyList(),
)

data class YearUiState(
    val year: Year,
    val nrOfPhoto: Int,
    val nrOfFavorites: Int
)

data class MonthUiState(
    val year: Year,
    val month: Month,
    val nrOfPhoto: Int,
    val nrOfUntriaged: Int,
    val nrOfTriaged: Int,
    val nrOfFavorites: Int
)

class OverviewScreenViewModel : ViewModel() {

    private val years = FotoDatabase.photos.findUniqueYears()

    val uiState = MutableStateFlow(OverviewScreenUiState())

    init {

        years.forEach { year ->
            val photosOfTheYear = FotoDatabase.photos.filterByYear(year)
            val nrOfPhotosPerYear = photosOfTheYear.size
            val nrOfFavorites: Int = photosOfTheYear.count { it.favorite }
            uiState.update {
                it.copy(
                    yearPhotos = it.yearPhotos.toMutableList().apply {
                        add(YearUiState(year, nrOfPhotosPerYear, nrOfFavorites))
                    }
                )
            }

            Month.entries.forEach { month ->
                val photosPerMonth = photosOfTheYear.filterByMonth(year, month)
                val nrOfPhoto = photosPerMonth.size
                val untriaged = photosPerMonth.filter { !it.triaged && !it.favorite }.size
                val triaged = photosPerMonth.count { it.triaged }
                val favorites: Int = photosPerMonth.count { it.favorite }

                uiState.update {
                    it.copy(
                        monthPhotos = it.monthPhotos.toMutableList().apply {
                            add(
                                MonthUiState(
                                    year,
                                    month,
                                    nrOfPhoto,
                                    untriaged,
                                    triaged,
                                    favorites,
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