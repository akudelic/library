package ecx.akudelic.library;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;

import ecx.akudelic.library.controller.LibraryController;

@SpringBootTest
@AutoConfigureMockMvc
class LibraryApplicationTests {

	@Autowired
	private LibraryController controller;

	@Autowired
	private MockMvc mockMvc;

	@Test
	void contextLoads() throws Exception{
		assertThat(controller).isNotNull();
	}

	@Test
	public void testLoadingOfLibraryView() throws Exception {
		this.mockMvc.perform(get("/")).andExpect(status().isOk())
			.andExpect(view().name("library/library.html"))
			.andDo(print());
	}

	@Test
	public void testNewBookLoan() throws Exception {
		this.mockMvc.perform(post("/loan-book")
			.param("userName", "User")
			.param("idBook", "bk1"))
			.andDo(print())
			.andExpect(redirectedUrl("/"))
			.andExpect(status().isFound())
			.andExpect(model().hasNoErrors());
	}

	@Test
	public void testReturnBook() throws Exception {
		this.mockMvc.perform(post("/return")
			.param("bookId", "bk1"))
			.andDo(print())
			.andExpect(redirectedUrl("/"))
			.andExpect(status().isFound())
			.andExpect(model().hasNoErrors());
	}
}
