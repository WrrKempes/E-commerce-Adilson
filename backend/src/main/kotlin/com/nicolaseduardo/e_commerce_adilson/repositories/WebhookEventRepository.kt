import com.nicolaseduardo.e_commerce_adilson.models.webhook.WebhookEvent
import org.springframework.data.jpa.repository.JpaRepository

interface WebhookEventRepository : JpaRepository<WebhookEvent, Long>