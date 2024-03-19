package pt.ulisboa.tecnico.tuplespaces.sequencer;

import io.grpc.stub.StreamObserver;
import pt.ulisboa.tecnico.sequencer.contract.SequencerGrpc.SequencerImplBase;
import pt.ulisboa.tecnico.sequencer.contract.SequencerOuterClass.GetSeqNumberRequest;
import pt.ulisboa.tecnico.sequencer.contract.SequencerOuterClass.GetSeqNumberResponse;

import java.util.List;

public class SequencerServiceImpl extends SequencerImplBase {

	int seqNumber;

	public SequencerServiceImpl() {
		seqNumber = 0;
	}

	@Override
	synchronized public void getSeqNumber(GetSeqNumberRequest request, StreamObserver<GetSeqNumberResponse> responseObserver) {
		seqNumber ++;
		GetSeqNumberResponse response = GetSeqNumberResponse.newBuilder()
				.setSeqNumber(seqNumber).build();
		responseObserver.onNext(response);
		responseObserver.onCompleted();
	}

}
