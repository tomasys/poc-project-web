package com.tomatosystem.core.tsv;

import java.io.IOException;
import java.io.Writer;
import java.util.function.Consumer;

/**
 * 이 스트림에 텍스트가 씌여지는 도중 개행 문제가 씌여질 때 마다 이전에 씌여진 한 행을 컨슈머에 콜백 합니다.
 * 
 * @see #withConsumer(Consumer)
 * 
 * @author jeeeyul
 *
 */
public class LineStream extends Writer {
	Consumer<String> fConsumer;
	StringBuffer fBuffer = new StringBuffer(4096);

	/**
	 * 한행이 씌여질 때마다 처리되는 컨슈머를 지정하고 쓰기 스트림을 작성 합니다.
	 * @param consumer 행 처리자.
	 * @return 쓰기 스트림.
	 */
	public static LineStream withConsumer(Consumer<String> consumer) {
		LineStream lineStream = new LineStream();
		lineStream.fConsumer = consumer;
		return lineStream;
	}

	private void consume() {
		if (fBuffer.length() == 0) {
			return;
		}
		String line = fBuffer.toString();
		fConsumer.accept(line);
		fBuffer.delete(0, line.length());
	}

	@Override
	public void write(char[] cbuf, int off, int len) throws IOException {
		if (len == 1 && cbuf[0] == '\n') {
			consume();
		} else {
			fBuffer.append(cbuf, off, len);
		}
	}

	@Override
	public void flush() throws IOException {
		consume();

	}

	@Override
	public void close() throws IOException {
		consume();
	}

}
