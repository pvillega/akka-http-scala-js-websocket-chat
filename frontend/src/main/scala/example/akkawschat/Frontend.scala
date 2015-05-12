package example.akkawschat

import org.scalajs.dom.raw._

import scala.scalajs.js
import org.scalajs.dom

object Frontend extends js.JSApp {
  val joinButton = dom.document.getElementById("join").asInstanceOf[HTMLButtonElement]
  val sendButton = dom.document.getElementById("send").asInstanceOf[HTMLButtonElement]
  val nameField = dom.document.getElementById("name").asInstanceOf[HTMLInputElement]
  val messageField = dom.document.getElementById("message").asInstanceOf[HTMLInputElement]
  val playground = dom.document.getElementById("playground")

  def main(): Unit = {
    joinButton.onclick = { (event: MouseEvent) ⇒
      joinChat(nameField.value)
      event.preventDefault()
    }

    nameField.focus()
    nameField.onkeypress = { (event: KeyboardEvent) ⇒
      if (event.keyCode == 13) {
        joinButton.click()
        event.preventDefault()
      }
    }

    messageField.onkeypress = { (event: KeyboardEvent) ⇒
      if (event.keyCode == 13) {
        sendButton.click()
        event.preventDefault()
      }
    }
  }

  def joinChat(name: String): Unit = {
    joinButton.disabled = true
    playground.innerHTML = s"Trying to join chat as '$name'..."

    // configure websocket
    val chat = new WebSocket(getWebsocketUri(dom.document, name))
    chat.onopen = { (event: Event) ⇒
      playground.insertBefore(p("Chat connection was successful!"), playground.firstChild)
      messageField.focus()

      sendButton.disabled = false
      sendButton.onclick = { (event: Event) ⇒
        chat.send(messageField.value)
        messageField.value = ""
        messageField.focus()
        event.preventDefault()
      }

      event
    }

    chat.onerror = { (event: ErrorEvent) ⇒
      closeChatMessage(s"Failed: code: ${event.colno}")
    }

    chat.onmessage = { (event: MessageEvent) ⇒
      playground.insertBefore(p(event.data.toString), playground.firstChild)
    }

    chat.onclose = { (event: Event) ⇒
      closeChatMessage("Connection to chat lost. You can try to rejoin manually.")
    }
  }

  private def closeChatMessage(msg: String) = {
    playground.insertBefore(p(msg), playground.firstChild)
    joinButton.disabled = false
    sendButton.disabled = true
  }

  private def getWebsocketUri(document: Document, nameOfChatParticipant: String): String = {
    val wsProtocol = if (dom.document.location.protocol == "https:") "wss" else "ws"

    s"$wsProtocol://${dom.document.location.host}/chat?name=$nameOfChatParticipant"
  }

  private def p(msg: String): Element = {
    val paragraph = dom.document.createElement("p")
    paragraph.innerHTML = msg
    paragraph
  }
}