import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfReader;
import com.itextpdf.kernel.pdf.canvas.parser.PdfTextExtractor;

import java.io.File;
import java.io.IOException;
import java.util.*;

public class BooleanSearchEngine implements SearchEngine {

    private Map<String, List<PageEntry>> listPageEntry = new HashMap<>();

    public BooleanSearchEngine(File pdfsDir) throws IOException {

        //читаем файлы и сохраняем их в список
        List<File> pdfFiles = new ArrayList<>();

        if (pdfsDir.isDirectory()) {
            for (File pdfFile : Objects.requireNonNull(pdfsDir.listFiles())) {
                if (pdfFile.getName().endsWith("pdf") && !pdfFile.isDirectory()) {
                    pdfFiles.add(new File(pdfsDir, pdfFile.getName()));
                }
            }
        } else {
            throw new IOException("Неверное имя директории");
        }

        // идем по списку файлов и считаем кол-во слов
        for (File pdf : pdfFiles) {
            var doc = new PdfDocument(new PdfReader(pdf));
            int pageNum = 1;

            while (pageNum != doc.getNumberOfPages() + 1) {

                var page = doc.getPage(pageNum);
                var text = PdfTextExtractor.getTextFromPage(page);
                var words = text.split("\\P{IsAlphabetic}+");

                Map<String, Integer> freqs = new HashMap<>();

                for (var word : words) {
                    if (word.isEmpty()) {
                        continue;
                    }
                    freqs.put(word.toLowerCase(), freqs.getOrDefault(word, 0) + 1);
                }
                // сохраняем список  слов с результатами поиска
                for (var entry : freqs.entrySet()) {
                    if (!listPageEntry.containsKey(entry.getKey())) {
                        listPageEntry.put(entry.getKey(), new ArrayList<>());
                    }
                    listPageEntry.get(entry.getKey()).add(new PageEntry(pdf.getName(), pageNum, entry.getValue()));
                }
                pageNum++;
            }
        }
    }

    @Override
    public List<PageEntry> search(String word) {
        List<PageEntry> pageEntryList = listPageEntry.get(word.toLowerCase());
        if (pageEntryList != null && !pageEntryList.isEmpty()) {
            Collections.sort(pageEntryList);
        } else {
            throw new NullPointerException("Такого слова нет в текстах");
        }
        return pageEntryList;
    }
}
