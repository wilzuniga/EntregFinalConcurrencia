import java.io.IOException;
import java.util.ArrayDeque;
import java.util.PriorityQueue;
import java.util.Queue;
import java.util.StringTokenizer;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.mapreduce.Reducer;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;

public class NGramCount {
    
    public static class NGramMapper extends Mapper<Object, Text, Text, IntWritable> {

        private final static IntWritable count = new IntWritable(1);
        private Text ngramText = new Text();
        private int nGramSize;
        private Queue<String> buffer;

        @Override
        protected void setup(Context context) throws IOException, InterruptedException {
            Configuration conf = context.getConfiguration();
            nGramSize = Integer.parseInt(conf.get("nGramSize", "1"));
            buffer = new ArrayDeque<>(nGramSize);
        }

        @Override
        public void map(Object key, Text value, Context context) throws IOException, InterruptedException {
            StringTokenizer tokenizer = new StringTokenizer(value.toString());

            while (tokenizer.hasMoreTokens()) {
                buffer.add(tokenizer.nextToken());
                if (buffer.size() == nGramSize) {
                    StringBuilder nGramBuilder = new StringBuilder();
                    for (String word : buffer) {
                        nGramBuilder.append(word).append(" ");
                    }
                    ngramText.set(nGramBuilder.toString().trim());
                    context.write(ngramText, count);
                    buffer.poll();
                }
            }
        }
    }

    public static class TopNGramsReducer extends Reducer<Text, IntWritable, Text, IntWritable> {

        private PriorityQueue<NGramCountPair> topNGrams;
        private int topCount;

        @Override
        protected void setup(Context context) {
            topCount = context.getConfiguration().getInt("topCount", 25);
            topNGrams = new PriorityQueue<>(topCount);
        }

        @Override
        public void reduce(Text key, Iterable<IntWritable> values, Context context) throws IOException, InterruptedException {
            int total = 0;
            for (IntWritable value : values) {
                total += value.get();
            }

            topNGrams.offer(new NGramCountPair(key.toString(), total));
            if (topNGrams.size() > topCount) {
                topNGrams.poll();
            }
        }

        @Override
        protected void cleanup(Context context) throws IOException, InterruptedException {
            while (!topNGrams.isEmpty()) {
        	NGramCountPair pair = topNGrams.poll();
        	// Formatear la salida con dos tabulaciones
        	String output = pair.ngram + "\t\t" + pair.count;
        	// Usar Text para la clave y escribir la salida
        	context.write(new Text(output), new IntWritable(0));
    	    }
        }

        private static class NGramCountPair implements Comparable<NGramCountPair> {
            String ngram;
            int count;

            NGramCountPair(String ngram, int count) {
                this.ngram = ngram;
                this.count = count;
            }

            @Override
            public int compareTo(NGramCountPair other) {
                return Integer.compare(this.count, other.count);
            }
        }
    }

    public static void main(String[] args) throws Exception {
        Configuration conf = new Configuration();

        Job job = Job.getInstance(conf, "ngram count");
        job.setJarByClass(NGramCount.class);

        int nGramSize = Integer.parseInt(args[2]);
        job.getConfiguration().setInt("nGramSize", nGramSize);

        job.setMapperClass(NGramMapper.class);
        job.setReducerClass(TopNGramsReducer.class);
        job.setOutputKeyClass(Text.class);
        job.setOutputValueClass(IntWritable.class);

        FileInputFormat.addInputPath(job, new Path(args[0]));
        FileOutputFormat.setOutputPath(job, new Path(args[1]));

        System.exit(job.waitForCompletion(true) ? 0 : 1);
    }
}
