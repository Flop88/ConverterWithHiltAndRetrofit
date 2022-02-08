package ge.gergmoney.myconverterpro

import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import ge.gergmoney.myconverterpro.extension.Resource
import ge.gergmoney.myconverterpro.extension.SingleLiveEvent
import ge.gergmoney.myconverterpro.model.ApiResponse
import ge.gergmoney.myconverterpro.repository.MainRepo
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import javax.inject.Inject


@HiltViewModel
class MainViewModel @Inject constructor(private val mainRepo: MainRepo) : ViewModel()  {
    //cached
    private val _data = SingleLiveEvent<Resource<ApiResponse>>()
    val data =  _data
    val convertedRate = MutableLiveData<Double>()

    val isPopularCountriesChecked = MutableLiveData(true)

    fun getConvertedData(access_key: String, from: String, to: String, amount: Double) {
        viewModelScope.launch {
            mainRepo.getConvertedData(access_key, from, to, amount).collect {
                data.value = it
            }
        }
    }

    fun changeCheckBox(value: Boolean) {
        isPopularCountriesChecked.value = value
    }
}
