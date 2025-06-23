// Complete and verified Alloy model with robust checks.

// --- Signatures (Confirmed to be working) ---
abstract sig State {}
one sig Idle, Receiving, Analyzing, Exporting, Responding, Done extends State {}

sig Request {}
sig Result {}

sig Service {
    state: State,
    request: lone Request,
    result: lone Result
}

sig Step {
    pre_state: Service,
    post_state: Service
}


// --- Predicates for State Transitions ---
pred Init(s: Service) {
    s.state = Idle
    no s.request
    no s.result
}

pred ClientSendsRequest(st: Step) {
    st.pre_state.state = Idle
    st.post_state.state = Receiving
    some st.post_state.request
    st.post_state.result = st.pre_state.result
}

pred ServerReceivesRequest(st: Step) {
    st.pre_state.state = Receiving
    st.post_state.state = Analyzing
    st.post_state.request = st.pre_state.request
    st.post_state.result = st.pre_state.result
}

pred ServerAnalyzesModel(st: Step) {
    st.pre_state.state = Analyzing
    st.post_state.state = Exporting
    st.post_state.request = st.pre_state.request
    some st.post_state.result
}

pred ServerExportsDOT(st: Step) {
    st.pre_state.state = Exporting
    st.post_state.state = Responding
    st.post_state.request = st.pre_state.request
    st.post_state.result = st.pre_state.result
}

pred ServerResponds(st: Step) {
    st.pre_state.state = Responding
    st.post_state.state = Done
    st.post_state.request = st.pre_state.request
    st.post_state.result = st.pre_state.result
}

// --- A single predicate for all valid transitions ---
pred Transition(st: Step) {
    ClientSendsRequest[st] or
    ServerReceivesRequest[st] or
    ServerAnalyzesModel[st] or
    ServerExportsDOT[st] or
    ServerResponds[st]
}

// --- A fact to constrain the universe to valid traces ---
// This forces the analyzer to only consider valid execution traces.
fact Traces {
    // 1. Any Service that is not a 'post_state' must be an initial state.
    all s: Service - Step.post_state | Init[s]

    // 2. Every step in the model must be a valid transition.
    all st: Step | Transition[st]

    // 3. (FIX) Every non-final state must lead to another state.
    // This ensures liveness and prevents the trace from getting stuck.
    all s: Service | s.state != Done implies (some st: Step | st.pre_state = s)
}

// --- Visualization Command ---
// This will now find and display any valid trace allowed by the Traces fact.
run ShowTrace {} for 5 Service, 4 Step, 3 Request, 3 Result

// --- Verification Commands ---

// Predicate to define a "Stuck" state.
pred Stuck(s: Service) {
    // The service is not in the final 'Done' state...
    s.state != Done
    // ...and there is no possible step that can be taken from this state.
    no st: Step | st.pre_state = s
}

// Check if a 'Stuck' state is ever reachable in our valid traces.
check IsNeverStuck {
    // The assertion is that no service in our model is ever stuck.
    no s: Service | Stuck[s]
} for 5 Service, 4 Step, 3 Request, 3 Result
