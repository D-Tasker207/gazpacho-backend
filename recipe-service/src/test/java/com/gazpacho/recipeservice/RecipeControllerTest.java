package com.gazpacho.recipeservice;

import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpHeaders;

import java.util.*;

import com.gazpacho.recipeservice.controller.RecipeController;
import com.gazpacho.recipeservice.model.RecipeEntity;
import com.gazpacho.recipeservice.service.RecipeService;
import com.gazpacho.sharedlib.dto.RecipeDTO;
import com.gazpacho.userservice.service.UserService;
import com.gazpacho.sharedlib.dto.PublicUserDTO;

@WebMvcTest(RecipeController.class)
public class RecipeControllerTest {

  @Autowired
  private MockMvc mockMvc;

  @MockitoBean
  private RecipeService recipeService;

  @MockitoBean
  UserService userService;

  @Nested
  @DisplayName("GET /recipes/{id}")
  class ViewRecipe {
    @Test
    //view recipe on an existing recipe
    @DisplayName("200 when recipe exists")
    void recipeFound() throws Exception {
      RecipeEntity r = new RecipeEntity(); r.setId(42L); r.setName("Gazpacho");
      given(recipeService.viewRecipe(42L)).willReturn(Optional.of(r));

      mockMvc.perform(get("/recipes/42"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.id").value(42))
        .andExpect(jsonPath("$.name").value("Gazpacho"));
    }

    @Test
    //view recipe on a nonexistent recipe
    @DisplayName("404 when recipe missing")
    void recipeMissing() throws Exception {
      given(recipeService.viewRecipe(99L)).willReturn(Optional.empty());

      mockMvc.perform(get("/recipes/99"))
        .andExpect(status().isNotFound())
        .andExpect(content().string("Recipe with id 99 not found"));
    }
  }

  @Nested
  @DisplayName("GET /recipes/search")
  class SearchRecipes {

        //Default test for search by name when no explicity type is passed
      @Test
      @DisplayName("defaults to recipe-name search when no type is given")
      void defaultSearchByName() throws Exception {
          RecipeDTO r1 = new RecipeDTO(
            1L,
            "Spaghetti",
            null,
            List.of(),
            Set.of(),
            List.of(),
            null,
            Set.of()
          );
          RecipeDTO r2 = new RecipeDTO(
            2L,
            "Spaghetti Bolognese",
            null,
            List.of(),
            Set.of(),
            List.of(),
            null, 
            Set.of()
          );
          when(recipeService.searchRecipes("spaghetti", "recipe"))
              .thenReturn(List.of(r1, r2));

          mockMvc.perform(get("/recipes/search").param("q", "spaghetti"))
              .andExpect(status().isOk())
              .andExpect(jsonPath("$.length()").value(2))
              .andExpect(jsonPath("$[0].id").value(1))
              .andExpect(jsonPath("$[0].name").value("Spaghetti"))
              .andExpect(jsonPath("$[1].id").value(2))
              .andExpect(jsonPath("$[1].name").value("Spaghetti Bolognese"));
      }

      @Test
      //searching by ingredient 
      @DisplayName("searches by ingredient when type=ingredient")
      void searchByIngredient() throws Exception {
          RecipeDTO cheeseDish = new RecipeDTO(
            3L,
            "Mac & Cheese",
            null,
            List.of(),
            Set.of(),
            List.of(),
            null,
            Set.of()
          );
          when(recipeService.searchRecipes("cheese", "ingredient"))
              .thenReturn(List.of(cheeseDish));

          mockMvc.perform(get("/recipes/search")
                  .param("q", "cheese")
                  .param("type", "ingredient"))
              .andExpect(status().isOk())
              .andExpect(jsonPath("$.length()").value(1))
              .andExpect(jsonPath("$[0].id").value(3))
              .andExpect(jsonPath("$[0].name").value("Mac & Cheese"));
      }

      @Test
      //searching by allergen
      @DisplayName("searches by allergen when type=allergen")
      void searchByAllergen() throws Exception {
          RecipeDTO peanutPie = new RecipeDTO(
            4L,
            "Peanut Pie",
            null,
            List.of(),
            Set.of(),
            List.of(),
            null,
            Set.of()
          );
          when(recipeService.searchRecipes("peanut", "allergen"))
              .thenReturn(List.of(peanutPie));

          mockMvc.perform(get("/recipes/search")
                  .param("q", "peanut")
                  .param("type", "allergen"))
              .andExpect(status().isOk())
              .andExpect(jsonPath("$.length()").value(1))
              .andExpect(jsonPath("$[0].id").value(4))
              .andExpect(jsonPath("$[0].name").value("Peanut Pie"));
      }

      @Test
      //test automatic fall back to recipe name when no type given 
      @DisplayName("unknown type falls back to recipe-name search")
      void unknownTypeFallback() throws Exception {
          RecipeDTO alfredo = new RecipeDTO(
            5L,
            "Chicken Alfredo",
            null,
            List.of(),
            Set.of(),
            List.of(),
            null,
            Set.of()
          );
          when(recipeService.searchRecipes("alfredo", "unknown"))
              .thenReturn(List.of(alfredo));

          mockMvc.perform(get("/recipes/search")
                  .param("q", "alfredo")
                  .param("type", "unknown"))
              .andExpect(status().isOk())
              .andExpect(jsonPath("$.length()").value(1))
              .andExpect(jsonPath("$[0].id").value(5))
              .andExpect(jsonPath("$[0].name").value("Chicken Alfredo"));
      }

      @Test
      //edge case for a search that yeilds no results
      @DisplayName("returns empty array when no recipes found")
      void returnsEmptyList() throws Exception {
          when(recipeService.searchRecipes("nothing", "recipe"))
              .thenReturn(Collections.emptyList());

          mockMvc.perform(get("/recipes/search")
                  .param("q", "nothing")
                  .param("type", "recipe"))
              .andExpect(status().isOk())
              .andExpect(jsonPath("$.length()").value(0));
      }
  }
    // -- Delete endpoint tests --
    @Nested
    @DisplayName("DELETE /recipes/{id} (admin only)")
    class DeleteRecipe {
      private final String goodToken = "Bearer valid.jwt.here";
  
      @Test
      @DisplayName("400 if no Authorization header")
      void noAuthHeader() throws Exception {
        mockMvc.perform(delete("/recipes/5"))
          .andExpect(status().isBadRequest());
      }
  
      @Test
      @DisplayName("403 if not a Bearer token")
      void badAuthHeader() throws Exception {
        mockMvc.perform(delete("/recipes/5")
            .header(HttpHeaders.AUTHORIZATION, "Basic foo"))
          .andExpect(status().isForbidden());
      }
  
      @Test
      @DisplayName("403 if user not admin")
      void nonAdmin() throws Exception {
        given(userService.fetchUser(goodToken))
          .willReturn(Optional.of(new PublicUserDTO(1L,"u@x.com",false,List.of())));
        mockMvc.perform(delete("/recipes/5")
            .header(HttpHeaders.AUTHORIZATION, goodToken))
          .andExpect(status().isForbidden());
      }
  
      @Test
      @DisplayName("200 if admin & delete succeeds")
      void adminCanDelete() throws Exception {
        given(userService.fetchUser(goodToken))
          .willReturn(Optional.of(new PublicUserDTO(1L,"a@x.com",true,List.of())));
        // no exception from service:
        Mockito.doNothing().when(recipeService).deleteRecipe(7L);
  
        mockMvc.perform(delete("/recipes/7")
            .header(HttpHeaders.AUTHORIZATION, goodToken))
          .andExpect(status().isOk())
          .andExpect(content().string("Recipe deleted successfully"));
      }
  
      @Test
      @DisplayName("404 if admin & delete throws \"not found\"")
      void adminDeleteNotFound() throws Exception {
        given(userService.fetchUser(goodToken))
          .willReturn(Optional.of(new PublicUserDTO(1L,"a@x.com",true,List.of())));
        Mockito.doThrow(new RuntimeException("not found"))
          .when(recipeService).deleteRecipe(8L);
  
        mockMvc.perform(delete("/recipes/8")
            .header(HttpHeaders.AUTHORIZATION, goodToken))
          .andExpect(status().isNotFound())
          .andExpect(content().string("Error deleting recipe: not found"));
      }
    }
  }