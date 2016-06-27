module Subscriptions exposing (subscriptions, websocketAddress)

import WebSocket
import Types exposing (..)

subscriptions : Model -> Sub Msg
subscriptions model =
  WebSocket.listen websocketAddress NewMessage

websocketAddress = "ws://localhost:3000/admin/ws"
