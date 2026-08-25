import { onMounted, onUnmounted, ref } from 'vue'
import { Client } from '@stomp/stompjs'
import SockJS from 'sockjs-client/dist/sockjs'
import { tokenStorage } from '@/utils/tokenStorage'

const WS_URL = import.meta.env.VITE_WS_URL ?? '/ws-green'

/**
 * Subscribes to the STOMP topics published by EcoPointsContainerActionService:
 *   /topic/eco-containers — updated container after a deposit or an emptying
 *   /topic/admin/alerts   — fired when a container crosses 90% full
 *
 * The backend registers a SockJS endpoint, so a raw WebSocket will not connect.
 *
 * Returns { connected } so views can show a reconnecting state instead of
 * silently displaying stale fullness values.
 */
export function useEcoContainerSocket({ onContainer, onAlert } = {}) {
  const connected = ref(false)
  let client = null

  onMounted(() => {
    client = new Client({
      webSocketFactory: () => new SockJS(WS_URL),
      reconnectDelay: 5000,

      /*
       * The SockJS handshake cannot carry an Authorization header, so the JWT
       * goes on the STOMP CONNECT frame instead. beforeConnect runs before
       * every attempt, so a reconnect after a token refresh uses the new token
       * rather than the one captured when the client was created.
       */
      beforeConnect: () => {
        const token = tokenStorage.getAccess()
        client.connectHeaders = token ? { Authorization: `Bearer ${token}` } : {}
      },

      onConnect: () => {
        connected.value = true

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
          client.subscribe('/topic/admin/alerts', (message) => {
            // The payload is a plain string today and a JSON object in the
            // newer backend, so accept either and let the view decide.
            try {
              onAlert(JSON.parse(message.body))
            } catch {
              onAlert(message.body)
            }
          })
        }
      },

      onDisconnect: () => {
        connected.value = false
      },
      onWebSocketClose: () => {
        connected.value = false
      },
      onStompError: () => {
        connected.value = false
      },
    })

    client.activate()
  })

  onUnmounted(() => client?.deactivate())

  return { connected }
}
