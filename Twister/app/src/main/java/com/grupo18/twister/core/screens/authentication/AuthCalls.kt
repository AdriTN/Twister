import android.os.Handler
import android.os.Looper
import com.grupo18.twister.core.api.ApiClient
import com.grupo18.twister.core.api.ApiService
import com.grupo18.twister.core.models.UserModel
import retrofit2.HttpException
import java.lang.Exception
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

class CreateUserTask(
    private val email: String,
    private val password: String,
    private val username: String,
    private val onResult: (Result<UserModel>) -> Unit
) {

    private val executor: ExecutorService = Executors.newSingleThreadExecutor()
    private val handler: Handler = Handler(Looper.getMainLooper())

    fun execute() {
        executor.execute {
            val result = try {
                val user = UserModel(username, email, password)
                val apiService = ApiClient.retrofit.create(ApiService::class.java)
                val response = apiService.createUser(user).execute() // Ejecuta la llamada de Retrofit sincrónicamente

                if (response.isSuccessful) {
                    response.body()?.let { Result.success(it) } ?: Result.failure(Exception("Respuesta vacía"))
                } else {
                    Result.failure(Exception("Error del servidor: ${response.code()}"))
                }
            } catch (e: HttpException) {
                Result.failure(Exception("Error de red: ${e.message()}"))
            } catch (e: Exception) {
                Result.failure(e)
            }

            handler.post {
                onResult(result)
            }
        }
    }
}
