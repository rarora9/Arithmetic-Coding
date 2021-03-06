package app;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

import ac.ArithmeticEncoder;
import io.OutputStreamBitSink;

public class StaticACEncodeVideoLinearDistribution {

	public static void main(String[] args) throws IOException {
		String input_file_name = "data/original.dat";
		String output_file_name = "data/encoded_linear.dat";

		int range_bit_width = 40;

		System.out.println("Encoding text file: " + input_file_name);
		System.out.println("Output file: " + output_file_name);
		System.out.println("Range Register Bit Width: " + range_bit_width);
		
		// Analyze file for frequency counts
		
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
		
		// Create new model with analyzed frequency counts
		
		FreqCountIntegerSymbolModel model = new FreqCountIntegerSymbolModel(symbols, symbol_counts);

		ArithmeticEncoder<Integer> encoder = new ArithmeticEncoder<Integer>(range_bit_width);

		FileOutputStream fos = new FileOutputStream(output_file_name);
		OutputStreamBitSink bit_sink = new OutputStreamBitSink(fos);

		// Now encode the input
		
		FileInputStream fis = new FileInputStream(input_file_name);
		
		int[][] previousFrame = new int[64][64];
		
		for(int i = 0; i < 64; i++) {
			for(int j = 0; j < 64; j++) {
				previousFrame[i][j] = fis.read();
				bit_sink.write(previousFrame[i][j], 8);
			}
		}
		
		for(int frame = 0; frame < 299; frame++) {
			for(int i = 0; i < 64; i++) {
				for(int j = 0; j < 64; j++) {
					int next_symbol = fis.read();
					encoder.encode(next_symbol - previousFrame[i][j], model, bit_sink);
					previousFrame[i][j] = next_symbol;
				}
			}
		}
		
		fis.close();

		// Finish off by emitting the middle pattern 
		// and padding to the next word
		
		encoder.emitMiddle(bit_sink);
		bit_sink.padToWord();
		fos.close();
		
		System.out.println("Done");
	}
}
