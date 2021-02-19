package ecx.akudelic.library.controller;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.annotation.PostConstruct;
import com.fasterxml.jackson.dataformat.xml.JacksonXmlModule;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;

import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

import ecx.akudelic.library.model.Book;
import ecx.akudelic.library.model.BookCatalog;
import ecx.akudelic.library.model.BookLoan;
import ecx.akudelic.library.model.User;

@Controller
public class LibraryController {

    private List<Book> allBooks;
    private List<BookLoan> loanRecords;
    private List<User> allUsers;

    private List<String> authors;
    private List<String> titles;

    private List<Book> freeBooks;
    private List<Book> filteredBooks;

    @PostConstruct
    public void init() {
        allBooks = new ArrayList<>();
        loanRecords = new ArrayList<>();
        allUsers = new ArrayList<>();

        authors = new ArrayList<>();
        titles = new ArrayList<>();

        freeBooks = new ArrayList<>();
        filteredBooks = new ArrayList<>();
    }
    
    /**
     * Method that loads data upon arriving on 'homepage'
     * @param model
     * @return String
     */
    @GetMapping("/")
    public String loadData(Model model){
        try {
            if (filteredBooks != null && !filteredBooks.isEmpty()){
                model.addAttribute("books", filteredBooks);
                filteredBooks = new ArrayList<>(); //For refreshing purposes
            } else {
                BookCatalog catalog= readXMLFile();
                allBooks = catalog.getAllBooks();
                freeBooks = new ArrayList<>();
    
                if (loanRecords.isEmpty()) {
                    freeBooks = allBooks;
                }
                else {
                    boolean bookLoaned = false;
                    for (Book book : allBooks) {
                        for (BookLoan loan : loanRecords){
                            if (book.getId().equals(loan.idBook)){
                                bookLoaned = true;
                            }
                        }
                        if (!bookLoaned){
                            freeBooks.add(book);
                        }
                        bookLoaned = false;
                    }
                }
                model.addAttribute("books", freeBooks);
            }
            
            populateAuthors(freeBooks);
            populateTitles(freeBooks);
            model.addAttribute("authors", authors);
            model.addAttribute("titles", titles);

            return "library/library.html";
        } catch (IOException e) {
            return "Exception while loading XML data!";
        }
    }

    /**
     * Method that stores book id when the form for the user is opened.
     * @param bookId
     * @param model
     * @return String
     */
    @PostMapping("/loan")
    public String loan(@RequestParam(value="bookId") String bookId, Model model){
        model.addAttribute("idBook", bookId);
        return "library/loan.html";
    }

    /**
     * Method that is used for loaning books by the user.
     * Also, creates user if it already hasn't been created.
     * @param userName
     * @param idBook
     * @param model
     * @return ModelAndView - redirect
     */
    @PostMapping("/loan-book")
    public ModelAndView loanBook(@RequestParam(value="userName") String userName,
            @RequestParam(value="idBook") String idBook, Model model){
        User user = null;
        if (allUsers != null && !allUsers.isEmpty()){
            for (User u : allUsers) {
                if (u.getUserName().equals(userName)){
                    user = u;
                }
            }
        }

        if (user == null){
            user = new User(userName);
        }

        BookLoan bookLoan = new BookLoan(idBook, user.getUserName());
        loanRecords.add(bookLoan);
        return new ModelAndView("redirect:/");
    }

    /**
     * Method that fetches only loaded books
     * @param model
     * @return String
     */
    @GetMapping("/loaned")
    public String getLoanedBooks(Model model){
        List<Book> loanedBooks = new ArrayList<>();
        if (this.loanRecords != null && !this.loanRecords.isEmpty()){
            for (Book book : this.allBooks) {
                for (BookLoan loan : loanRecords) {
                    if (book.getId().equals(loan.idBook) && !loanedBooks.contains(book)) {
                        loanedBooks.add(book);
                    }
                }               
            }
        }
        else {
            loanedBooks = null;
        }
        model.addAttribute("loans", loanRecords);
        model.addAttribute("loanedBooks", loanedBooks);
        return "library/loaned-book.html";
    }

    /**
     * Method for filtering data in data table.
     * @param author
     * @param title
     * @param description
     * @param dateFrom
     * @param dateTo
     * @param model
     * @return ModelAndView - redirect
     */
    @PostMapping("/filter")
    public ModelAndView filterBooks(@RequestParam(value="author", required = false) String author,
     @RequestParam(value="title", required = false) String title,
     @RequestParam(value="description", required = false) String description,
     @RequestParam(value="dateFrom", required = false) String dateFrom,
     @RequestParam(value="dateTo", required = false) String dateTo,
     Model model){
        if (!author.equals("")) {
            for (Book book : freeBooks) {
                if (book.getAuthor().equals(author.trim())){
                    if (!filteredBooks.contains(book))
                        filteredBooks.add(book);
                }
            }
        }

        if (!title.equals("")){
            for (Book book : freeBooks) {
                if (book.getTitle().equals(title.trim())){
                    if (!filteredBooks.contains(book)) 
                        filteredBooks.add(book);
                }
            }
        }

        if (!description.equals("")){
            for (Book book : freeBooks) {
                if (book.getDescription().contains(description.trim())){
                    if (!filteredBooks.contains(book))
                        filteredBooks.add(book);
                }
            }
        }

        if (!dateFrom.equals("")){
            for (Book book : freeBooks) {
                Date bookDate = convertToDate(book.getPublishDate());
                Date fromDate = convertToDate(dateFrom);
                if (!dateTo.equals("")){
                    Date toDate = convertToDate(dateTo);
                    if (fromDate.compareTo(bookDate) <= 0 && toDate.compareTo(bookDate) >=0){
                        if (!filteredBooks.contains(book))
                            filteredBooks.add(book);
                    }
                }
                else {
                    if (fromDate.compareTo(bookDate) <= 0){
                        if (!filteredBooks.contains(book))
                            filteredBooks.add(book);
                    }
                }
            }
        }

        if (!dateTo.equals("")){
            for (Book book : freeBooks) {
                Date bookDate = convertToDate(book.getPublishDate());
                Date toDate = convertToDate(dateTo);
                if (!dateFrom.equals("")){
                    Date fromDate = convertToDate(dateFrom);
                    if (fromDate.compareTo(bookDate) <= 0 && toDate.compareTo(bookDate) >=0){
                        if (!filteredBooks.contains(book))
                            filteredBooks.add(book);
                    }
                }
                else {
                    if (toDate.compareTo(bookDate) >= 0){
                        if (!filteredBooks.contains(book))
                            filteredBooks.add(book);
                    }
                }
            }
        }
        return new ModelAndView("redirect:/");
    }
    
    /**
     * Method for returning books to library
     * @param idBook
     * @param model
     * @return ModelAndView - redirect to 'homepage'
     */
    @PostMapping("/return")
    public ModelAndView returnBook(@RequestParam(value="bookId") String idBook, Model model){
        for (BookLoan loan : loanRecords) {
            if (loan.idBook.equals(idBook)){
                loanRecords.remove(loan);
                break;
            }
        }
        return new ModelAndView("redirect:/");
    }

    /**
     * Helper method for reading XML file
     * @return BookCatalog
     * @throws IOException
     */
    private BookCatalog readXMLFile() throws IOException {
        File xmlFile = new File(new ClassPathResource("/src/main/resources/books.xml").getPath());

        JacksonXmlModule module = new JacksonXmlModule();
        module.setDefaultUseWrapper(false);
        String xml = streamToString(new FileInputStream(xmlFile));

        XmlMapper xmlMapper = new XmlMapper(module);
        return xmlMapper.readValue(xml, BookCatalog.class);
    }

    /**
     * Helper method for converting input stream to string
     * @param is
     * @return String
     * @throws IOException
     */
    private String streamToString(InputStream is) throws IOException{
        StringBuilder sb = new StringBuilder();
        String line;
        BufferedReader br = new BufferedReader(new InputStreamReader(is));
        while((line = br.readLine()) != null){
            sb.append(line);
        }
        br.close();
        return sb.toString();
    }

    /**
     * Helper method for populating authors
     * @param books
     */
    private void populateAuthors(List<Book> books){
        for (Book book : books) {
            if (!authors.contains(book.getAuthor())){
                authors.add(book.getAuthor());
            }
        }
    }

    /**
     * Helper method for populating titles
     * @param books
     */
    private void populateTitles(List<Book> books){
        for (Book book : books) {
            if (!titles.contains(book.getTitle())){
                titles.add(book.getTitle());
            }
        }
    }

    private Date convertToDate(String date){
        Date convertedDate = new Date();
        try{
            convertedDate = new SimpleDateFormat("yyyy-MM-dd").parse(date);
        } 
        catch (ParseException pe) {
            System.out.println("Exception while parsing string to date!");
        }
        return convertedDate;
    }
}
