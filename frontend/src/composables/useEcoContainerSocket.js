import { onMounted, onUnmounted } from 'vue'
import { Client } from '@stomp/stompjs'
import SockJS from 'sockjs-client/dist/sockjs'

const WS_URL = import.meta.env.VITE_WS_URL ?? '/ws-green'

/**
 * Subscribes to the STOMP topics published by EcoPointsContainerActionService:
 *   /topic/eco-containers — EcoPointContainerResponseDto after every deposit
 *   /topic/admin/alerts   — plain string when a container passes 90% full
 *
 * The backend registers a SockJS endpoint, so a raw WebSocket will not connect.
 */
export function useEcoContainerSocket({ onContainer, onAlert } = {}) {
  let client = null

  onMounted(() => {
    client = new Client({
      webSocketFactory: () => new SockJS(WS_URL),
      reconnectDelay: 5000,
      onConnect: () => {
        if (onContainer) {
          client.subscribe('/topic/eco-containers', (message) => {
            try {
              onContainer(JSON.parse(message.body))
            } catch {
              // Ignore malformed frames rather than tearing down the socket.
            }
          })
        }
        if (onAlert) {
          client.subscribe('/topic/admin/alerts', (message) => onAlert(message.body))
        }
      },
    })
    client.activate()
  })

  onUnmounted(() => client?.deactivate())
}
