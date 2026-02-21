import com.nicolaseduardo.e_commerce_adilson.models.coupon.OrderCoupon
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface OrderCouponRepository : JpaRepository<OrderCoupon, Long> {
    fun findByOrderId(orderId: Long): List<OrderCoupon>
}