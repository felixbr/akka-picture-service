module App exposing (main)

import Html exposing (..)
import Html.App as Html
import Html.Attributes exposing (..)
import Html.Events exposing (..)
import Task exposing (Task)

import State
import Types
import View
import Subscriptions


main =
  Html.program
    { init = State.initialState
    , update = State.update
    , view = View.root
    , subscriptions = Subscriptions.subscriptions
    }


-- port tasks : Signal (Task Never ())
-- port tasks =
--   app.tasks
