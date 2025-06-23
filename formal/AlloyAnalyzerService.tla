---- MODULE AlloyAnalyzerService ----
EXTENDS Naturals, Sequences

CONSTANTS Client, Server, Alloy, DotExporter

NatSet == 0..2
MaxLen == 2

\* Explicitly enumerate all sequences of NatSet up to length 2
SeqSet == {
  <<>>,
  <<0>>, <<1>>, <<2>>,
  <<0,0>>, <<0,1>>, <<0,2>>,
  <<1,0>>, <<1,1>>, <<1,2>>,
  <<2,0>>, <<2,1>>, <<2,2>>
}

VARIABLES state, request, result

(*
States:
  "Idle"         : Waiting for client connection
  "Receiving"    : Receiving Alloy model and command
  "Analyzing"    : Parsing and analyzing model
  "Exporting"    : Converting result to DOT
  "Responding"   : Streaming result back to client
  "Done"         : Request complete
*)

State == {"Idle", "Receiving", "Analyzing", "Exporting", "Responding", "Done"}

Init == 
  /\ state = "Idle"
  /\ request = << >>
  /\ result = << >>

ClientSendsRequest ==
  /\ state = "Idle"
  /\ state' = "Receiving"
  /\ request' \in SeqSet
  /\ UNCHANGED result

ServerReceivesRequest ==
  /\ state = "Receiving"
  /\ state' = "Analyzing"
  /\ UNCHANGED <<request, result>>

ServerAnalyzesModel ==
  /\ state = "Analyzing"
  /\ state' = "Exporting"
  /\ result' \in SeqSet
  /\ UNCHANGED request

ServerExportsDOT ==
  /\ state = "Exporting"
  /\ state' = "Responding"
  /\ UNCHANGED <<request, result>>

ServerResponds ==
  /\ state = "Responding"
  /\ state' = "Done"
  /\ UNCHANGED <<request, result>>

\* FIX: Add a stuttering step for the terminal state.
\* This action is only enabled when the process is finished.
DoneStutter ==
  /\ state = "Done"
  /\ UNCHANGED <<state, request, result>>

Next ==
  \/ ClientSendsRequest
  \/ ServerReceivesRequest
  \/ ServerAnalyzesModel
  \/ ServerExportsDOT
  \/ ServerResponds
  \/ DoneStutter  \* Add the stuttering action to the Next predicate

Spec == Init /\ [][Next]_<<state, request, result>>

====
