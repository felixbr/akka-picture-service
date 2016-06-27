module Types exposing (..)

type alias Model =
  { input : String
  , messages : List String
  }

type Action = NoOp

type Msg
  = Input String
  | Send
  | NewMessage String
