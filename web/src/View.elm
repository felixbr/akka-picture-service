module View exposing (root)

import Html exposing (..)
import Html.Attributes exposing (..)
import Html.Events exposing (..)
import String
import Types exposing (..)

root : Model -> Html Msg
root model =
  div []
    [ input [onInput Input, value model.input] []
    , button [onClick Send, disabled (sendingDisallowed model.input)] [text "Send"]
    , div [] (List.map viewMessage model.messages)
    ]


viewMessage : String -> Html msg
viewMessage msg =
  div [] [ text msg ]


sendingDisallowed input =
  String.isEmpty input
