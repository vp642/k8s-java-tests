package vlad;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.time.Duration;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Flow.Publisher;
import java.util.concurrent.Flow.Subscriber;
import java.util.concurrent.TimeUnit;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Scheduler;
import reactor.core.scheduler.Schedulers;

public class ReactorFileReadTest {

	public static void main(String[] args) throws IOException, InterruptedException {
		// TODO Auto-generated method stub

		
		if( args.length == 0 ) {
			System.out.println("Usage: ReactorFileReader {input-file-name}");
			return;
		}
		BufferedReader reader = new BufferedReader( new FileReader( args[0] ));
		Flux.fromStream( reader.lines() ).subscribe( line -> System.out.println(line));
		reader.close();
		
		
// 		Process proc = Runtime.getRuntime().exec( "powershell C:\\Users\\du82\\Documents\\dev\\k8sexec\\test-script.ps1" );
		
		ProcessBuilder procBuilder = new ProcessBuilder("PowerShell","C:\\\\Users\\\\du82\\\\Documents\\\\dev\\\\k8sexec\\\\test-script.ps1");
//		procBuilder.redirectErrorStream(true);
		Process proc = procBuilder.start();
		// CompletableFuture<Integer> exitValueFuture = new CompletableFuture<> ();

		// Mono<String> exitValue = Mono.fromFuture(exitValueFuture).map( val -> { return "? " + val; } );
		
		Mono<String> exitValueMono = Mono.fromSupplier( () -> {
						try {
							proc.waitFor();
						} catch (InterruptedException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
						return Integer.toString( proc.exitValue());
					}).map( val -> { return "e " + val; } );
		
		Flux<String> stdout = Flux.fromStream( new BufferedReader( new InputStreamReader(proc.getInputStream())).lines())
				.map( line -> { return "1 " + line; } ).subscribeOn( Schedulers.parallel());

		Flux<String> stderr = Flux.fromStream( new BufferedReader( new InputStreamReader(proc.getErrorStream())).lines())
				.map( line -> { return "2 " + line; } )
				.map( line -> line.replaceAll("At line:1","--->"))
				.subscribeOn( Schedulers.parallel());

		Flux.merge( stdout,stderr, exitValueMono )
			.timeout(Duration.ofSeconds(6)/*, Mono.just("the script has timed out")*/)
			.doOnError( err -> proc.destroy())
			.subscribe( line -> { 
				if( line.matches("^e ") )
					System.out.println( "EXIT CODE: " + line );
				else
					System.out.println( line ); 
			},
				err -> { 
					// proc.destroy(); 
					System.err.println(  "ERROR: " + err );
				}, 
					()-> System.out.println("Exit code: " + proc.exitValue()));

/*		
		Thread readerThread = new Thread( new Runnable() {
			public void run() {
				BufferedReader reader = new BufferedReader( new InputStreamReader( proc.getInputStream()));
				String line;
				
				try {
					while( (line=reader.readLine()) != null ) {
						System.out.println( line );
					}
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				try {
					reader.close();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		});
		
		readerThread.start();

		Thread errReaderThread = new Thread( new Runnable() {
			public void run() {
				BufferedReader reader = new BufferedReader( new InputStreamReader( proc.getErrorStream()));
				String line;
				
				try {
					while( (line=reader.readLine()) != null ) {
						System.out.println( "stderr: " + line );
					}
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				try {
					reader.close();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		});
		
		errReaderThread.start();

		
		readerThread.join();
		errReaderThread.join();
	*/	
		// proc.waitFor( 5, TimeUnit.SECONDS);
		proc.waitFor( );
		
		// exitValueFuture.complete( proc.exitValue() );
		
		//exitCodeFuture.complete( Integer.toString( proc.exitValue() ));
	}

}

/*
class filePublisher implements Publisher {

	Subcriber 
	@Override
	public void subscribe(Subscriber subscriber) {
		
		
	}
	
}*/
