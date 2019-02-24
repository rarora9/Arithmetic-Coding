package app;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

import ac.ArithmeticDecoder;
import io.InputStreamBitSource;
import io.InsufficientBitsLeftException;

public class StaticACDecodeVideoLinearDistribution {

	public static void main(String[] args) throws InsufficientBitsLeftException, IOException {
		String input_file_name = "data/encoded_linear.dat";
		String output_file_name = "data/decoded_linear.dat";

		FileInputStream fis = new FileInputStream(input_file_name);

		InputStreamBitSource bit_source = new InputStreamBitSource(fis);

		// Read in symbol counts and set up model
		
		int[] symbol_counts = new int[511];
		Integer[] symbols = new Integer[511];
		
		int count = 1;
		boolean beforePeak = true;
		
		for(int i = 0; i < 511; i++) {
			symbol_counts[i] = count;
			
			if(count == 256) {
				beforePeak = false;
			}
			
			if(beforePeak) {
				count++;
			} else {
				count--;
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
}
