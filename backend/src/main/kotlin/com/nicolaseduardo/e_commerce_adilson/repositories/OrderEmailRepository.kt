import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface OrderEmailRepository : JpaRepository<OrderEmail, Long> {
    fun findByOrderId(orderId: Long): List<OrderEmail>
    fun findByOrderIdAndEmailType(orderId: Long, emailType: String): List<OrderEmail>
    fun findByEmailType(emailType: String): List<OrderEmail>
}