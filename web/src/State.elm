module State exposing (initialState, update)

import Types exposing (..)
import Subscriptions exposing (websocketAddress)
import WebSocket
import Debug

initialState : (Model, Cmd msg)
initialState = (Model "" [], Cmd.none)

update : Msg -> Model -> (Model, Cmd Msg)
update msg model =
  case msg of
    Input newInput ->
      (Model (Debug.log "input" newInput) model.messages, Cmd.none)

    Send ->
      (Model "" model.messages, WebSocket.send websocketAddress (Debug.log "model.input" model.input))

    NewMessage str ->
      (Model model.input (str :: model.messages), Cmd.none)
