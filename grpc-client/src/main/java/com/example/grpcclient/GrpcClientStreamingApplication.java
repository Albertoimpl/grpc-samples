package com.example.grpcclient;

import com.example.grpcserver.hello.HelloRequest;
import com.example.grpcserver.hello.HelloResponse;
import com.example.grpcserver.hello.HelloServiceGrpc;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.Server;
import io.grpc.ServerBuilder;
import io.grpc.Status;
import io.grpc.netty.shaded.io.grpc.netty.GrpcSslContexts;
import io.grpc.netty.shaded.io.grpc.netty.NettyChannelBuilder;
import io.grpc.stub.ClientCallStreamObserver;
import io.grpc.stub.ClientResponseObserver;
import io.grpc.stub.ServerCallStreamObserver;
import io.grpc.stub.StreamObserver;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import javax.net.ssl.SSLException;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import java.security.cert.X509Certificate;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static io.grpc.netty.shaded.io.grpc.netty.NegotiationType.TLS;

//@SpringBootApplication
public class GrpcClientStreamingApplication
		implements ApplicationRunner {

		public static void main(String[] args) {
				SpringApplication.run(GrpcClientStreamingApplication.class, args);
		}

		@Override public void run(ApplicationArguments args)
				throws SSLException, InterruptedException {
				final CountDownLatch done = new CountDownLatch(1);

				int gatewayPort = 8090;
				int serverPort = 6565;
//				        ManagedChannel channel = createPlainTextChannel(gatewayPort);
//				        ManagedChannel channel = createSecuredChannel(50052);
				ManagedChannel channel = createSecuredChannel(gatewayPort);
				HelloServiceGrpc.HelloServiceStub stub = HelloServiceGrpc.newStub(
						channel);

				// When using manual flow-control and back-pressure on the client, the ClientResponseObserver handles both
				// request and response streams.
				ClientResponseObserver<HelloRequest, HelloResponse> clientResponseObserver = new ClientResponseObserver<HelloRequest, HelloResponse>() {

						ClientCallStreamObserver<HelloRequest> requestStream;

						@Override public void beforeStart(
								final ClientCallStreamObserver<HelloRequest> requestStream) {
								this.requestStream = requestStream;
								// Set up manual flow control for the response stream. It feels backwards to configure the response
								// stream's flow control using the request stream's observer, but this is the way it is.
								requestStream.disableAutoRequestWithInitial(1);

								// Set up a back-pressure-aware producer for the request stream. The onReadyHandler will be invoked
								// when the consuming side has enough buffer space to receive more messages.
								//
								// Messages are serialized into a transport-specific transmit buffer. Depending on the size of this buffer,
								// MANY messages may be buffered, however, they haven't yet been sent to the server. The server must call
								// request() to pull a buffered message from the client.
								//
								// Note: the onReadyHandler's invocation is serialized on the same thread pool as the incoming
								// StreamObserver's onNext(), onError(), and onComplete() handlers. Blocking the onReadyHandler will prevent
								// additional messages from being processed by the incoming StreamObserver. The onReadyHandler must return
								// in a timely manner or else message processing throughput will suffer.
								requestStream.setOnReadyHandler(new Runnable() {
										// An iterator is used so we can pause and resume iteration of the request data.
										Iterator<String> iterator = names().iterator();

										@Override public void run() {
												// Start generating values from where we left off on a non-gRPC thread.
												while (requestStream.isReady()) {
														if (iterator.hasNext()) {
																// Send more messages if there are more messages to send.
																String name = iterator.next();
																System.out.println(
																		"--> " + name);
																HelloRequest request = HelloRequest.newBuilder()
																		.setFirstName(name)
																		.build();
																requestStream.onNext(
																		request);
														}
														else {
																// Signal completion if there is nothing left to send.
																requestStream.onCompleted();
														}
												}
										}
								});
						}

						@Override public void onNext(HelloResponse value) {
								System.out.println("<-- " + value.getGreeting());
								// Signal the sender to send one message.
								requestStream.request(1);
						}

						@Override public void onError(Throwable t) {
								t.printStackTrace();
								done.countDown();
						}

						@Override public void onCompleted() {
								System.out.println("All Done");
								done.countDown();
						}
				};

				// Note: clientResponseObserver is handling both request and response stream processing.
				stub.helloStream(clientResponseObserver);

				done.await();

				channel.shutdown();
				channel.awaitTermination(1, TimeUnit.SECONDS);
		}

		private static List<String> names() {
				return Arrays.asList("Sophia", "Jackson", "Emma", "Aiden", "Olivia",
						"Lucas", "Ava", "Liam", "Mia", "Noah", "Isabella", "Ethan",
						"Riley", "Mason", "Aria", "Caden", "Zoe", "Oliver", "Charlotte",
						"Elijah", "Lily", "Grayson", "Layla", "Jacob", "Amelia",
						"Michael", "Emily", "Benjamin", "Madelyn", "Carter", "Aubrey",
						"James", "Adalyn", "Jayden", "Madison", "Logan", "Chloe",
						"Alexander", "Harper", "Caleb", "Abigail", "Ryan", "Aaliyah",
						"Luke", "Avery", "Daniel", "Evelyn", "Jack", "Kaylee", "William",
						"Ella", "Owen", "Ellie", "Gabriel", "Scarlett", "Matthew",
						"Arianna", "Connor", "Hailey", "Jayce", "Nora", "Isaac",
						"Addison", "Sebastian", "Brooklyn", "Henry", "Hannah", "Muhammad",
						"Mila", "Cameron", "Leah", "Wyatt", "Elizabeth", "Dylan", "Sarah",
						"Nathan", "Eliana", "Nicholas", "Mackenzie", "Julian", "Peyton",
						"Eli", "Maria", "Levi", "Grace", "Isaiah", "Adeline", "Landon",
						"Elena", "David", "Anna", "Christian", "Victoria", "Andrew",
						"Camilla", "Brayden", "Lillian", "John", "Natalie", "Lincoln");
		}

		private ManagedChannel createPlainTextChannel(int port) {
				return ManagedChannelBuilder.forAddress("localhost", port).usePlaintext()
						.build();
		}

		private ManagedChannel createSecuredChannel(int port) throws SSLException {
				TrustManager[] trustAllCerts = new TrustManager[] {
						new X509TrustManager() {
								public X509Certificate[] getAcceptedIssuers() {
										return new X509Certificate[0];
								}

								public void checkClientTrusted(X509Certificate[] certs,
										String authType) {
								}

								public void checkServerTrusted(X509Certificate[] certs,
										String authType) {
								}
						} };

				ManagedChannel channel = NettyChannelBuilder.forAddress("localhost", port)
						.useTransportSecurity().sslContext(
								GrpcSslContexts.forClient().trustManager(trustAllCerts[0])
										.build()).negotiationType(TLS).build();
				return channel;
		}
}
