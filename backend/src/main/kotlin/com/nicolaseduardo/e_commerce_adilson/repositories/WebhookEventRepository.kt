import org.springframework.data.jpa.repository.JpaRepository

interface WebhookEventRepository : JpaRepository<WebhookEvent, Long>