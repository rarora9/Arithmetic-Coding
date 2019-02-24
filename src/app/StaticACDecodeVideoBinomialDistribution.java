package app;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.math.BigInteger;

import ac.ArithmeticDecoder;
import io.InputStreamBitSource;
import io.InsufficientBitsLeftException;

public class StaticACDecodeVideoBinomialDistribution {

	public static void main(String[] args) throws InsufficientBitsLeftException, IOException {
		String input_file_name = "data/encoded_binomial.dat";
		String output_file_name = "data/decoded_binomial.dat";

		FileInputStream fis = new FileInputStream(input_file_name);

		InputStreamBitSource bit_source = new InputStreamBitSource(fis);

		// Read in symbol counts and set up model
		
		int[] symbol_counts = new int[511];
		Integer[] symbols = new Integer[511];
		
		for(int i = 0; i < 511; i++) {
			symbol_counts[i] = (int) ((nCk(510, i).doubleValue() * Math.pow(0.5, 510)) * 2000000000.0);
			
			if(symbol_counts[i] < 1) {
				symbol_counts[i] = 1;
			}
		}
		
		for(int i = 0; i < 511; i++) {
			symbols[i] = i - 255;
		}

		FreqCountIntegerSymbolModel model = new FreqCountIntegerSymbolModel(symbols, symbol_counts);
		
		int[][] previousFrame = new int[64][64];
		
		for(int i = 0; i < 64; i++) {
			for(int j = 0; j < 64; j++) {
				previousFrame[i][j] = fis.read();
			}
		}
		
		// Read in range bit width and setup the decoder

		int range_bit_width = 40;
		ArithmeticDecoder<Integer> decoder = new ArithmeticDecoder<Integer>(range_bit_width);

		// Decode and produce output.
		
		System.out.println("Uncompressing file: " + input_file_name);
		System.out.println("Output file: " + output_file_name);
		System.out.println("Range Register Bit Width: " + range_bit_width);
		
		FileOutputStream fos = new FileOutputStream(output_file_name);
		
		for(int i = 0; i < 64; i++) {
			for(int j = 0; j < 64; j++) {
				fos.write(previousFrame[i][j]);
			}
		}
		
		for(int frame = 0; frame < 299; frame++) {
			for(int i = 0; i < 64; i++) {
				for(int j = 0; j < 64; j++) {
					int sym = decoder.decode(model, bit_source);
					previousFrame[i][j] += sym;
					fos.write(previousFrame[i][j]);
				}
			}
		}

		System.out.println("Done.");
		fos.flush();
		fos.close();
		fis.close();
	}
	
	public static BigInteger factorial(int x) {
		BigInteger answer = BigInteger.valueOf(1);
		
		for(int i = 1; i <= x; i++) {
			answer = answer.multiply(BigInteger.valueOf(i));
		}
	    
		return answer;
	}
	
	public static BigInteger nCk(int n, int k) {
		return factorial(n).divide(factorial(k).multiply(factorial(n - k)));
	}
}
