package ge.gergmoney.myconverterpro.repository

import ge.gergmoney.myconverterpro.extension.Resource
import ge.gergmoney.myconverterpro.model.ApiResponse
import ge.gergmoney.myconverterpro.network.BaseDataSource
import ge.gergmoney.myconverterpro.network.DataSource
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import javax.inject.Inject

class MainRepo @Inject constructor(private val apiDataSource: DataSource) : BaseDataSource() {
    suspend fun getConvertedData(access_key: String, from: String, to: String, amount: Double): Flow<Resource<ApiResponse>> {
        return flow {
            emit(safeApiCall { apiDataSource.getConvertedRate(access_key, from, to, amount) })
        }.flowOn(Dispatchers.IO)
    }
}