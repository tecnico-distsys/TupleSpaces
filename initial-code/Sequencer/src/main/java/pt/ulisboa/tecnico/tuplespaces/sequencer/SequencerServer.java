package pt.ulisboa.tecnico.tuplespaces.sequencer;

import io.grpc.BindableService;
import io.grpc.Server;
import io.grpc.ServerBuilder;

public class SequencerServer {

	/** Server host port. */
	private static int port;

	public static void main(String[] args) throws Exception {
		System.out.println(SequencerServer.class.getSimpleName());

		// Print received arguments.
		System.out.printf("Received %d arguments%n", args.length);
		for (int i = 0; i < args.length; i++) {
			System.out.printf("arg[%d] = %s%n", i, args[i]);
		}

		// Check arguments.
		if (args.length < 1) {
			System.err.println("Argument(s) missing!");
			System.err.printf("Usage: java %s port%n", Server.class.getName());
			return;
		}

		port = Integer.valueOf(args[0]);
		final BindableService impl = new SequencerServiceImpl();

		// Create a new server to listen on port.
		Server server = ServerBuilder.forPort(port).addService(impl).build();
		// Start the server.
		server.start();
		// Server threads are running in the background.
		System.out.println("Sequencer server started");

		// Do not exit the main thread. Wait until server is terminated.
		server.awaitTermination();

		// Server is terminated.
		System.out.println("Sequencer server stopped");
		server.shutdown();
	}

}
