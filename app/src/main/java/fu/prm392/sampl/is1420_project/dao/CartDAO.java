package fu.prm392.sampl.is1420_project.dao;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.SetOptions;
import com.google.firebase.firestore.Transaction;
import com.google.firebase.firestore.WriteBatch;

import java.util.HashMap;
import java.util.Map;

import fu.prm392.sampl.is1420_project.dto.BasketDTO;
import fu.prm392.sampl.is1420_project.dto.BasketItemDTO;
import fu.prm392.sampl.is1420_project.dto.CartDTO;
import fu.prm392.sampl.is1420_project.dto.FoodDTO;

public class CartDAO {
    private FirebaseFirestore db;

    public CartDAO() {
        db = FirebaseFirestore.getInstance();
    }


    public Task<Void> addToCart(CartDTO cartDTO, BasketDTO basketDTO, BasketItemDTO basketItemDTO, FoodDTO foodDTO) {
        DocumentReference basketItemRef = db.collection("basketItems").document();
        DocumentReference basketRef = db.collection("baskets").document();
        DocumentReference cartRef = db.collection("carts").document();
        basketItemDTO.setBasketItemID(basketItemRef.getId());
        basketDTO.setBasketID(basketRef.getId());
        cartDTO.setCartID(cartRef.getId());
        WriteBatch batch = db.batch();
        Map<String, Object> dataCart = new HashMap<>();
        dataCart.put("cartsInfo", cartDTO);
        batch.set(cartRef, dataCart, SetOptions.merge());

        Map<String, Object> dataBasket = new HashMap<>();
        dataBasket.put("basketsInfo", basketDTO);
        batch.set(basketRef, dataBasket, SetOptions.merge());

        Map<String, Object> dataBasketItem = new HashMap<>();
        dataBasketItem.put("basketItemsInfo", basketItemDTO);
        batch.set(basketItemRef, dataBasketItem, SetOptions.merge());

        DocumentReference basketInCartRef = db.collection("carts")
                .document(cartDTO.getCartID());
        Map<String, Object> dataInCart = new HashMap<>();
        dataInCart.put("basketsInfo", FieldValue.arrayUnion(basketDTO));
        batch.update(basketInCartRef, dataInCart);

        DocumentReference basketItemInBasketRef = db.collection("baskets")
                .document(basketDTO.getBasketID());
        Map<String, Object> dataInBasket = new HashMap<>();
        dataInBasket.put("basketItemsInfo", FieldValue.arrayUnion(basketItemDTO));
        batch.update(basketItemInBasketRef, dataInBasket);

        DocumentReference cartInBasketRef = db.collection("baskets")
                .document(basketDTO.getBasketID());
        Map<String, Object> dataCartInBasket = new HashMap<>();
        dataCartInBasket.put("cartsInfo", cartDTO);
        batch.update(cartInBasketRef, dataCartInBasket);


        DocumentReference foodInBasketItemRef = db.collection("basketItems")
                .document(basketItemDTO.getBasketItemID());
        Map<String, Object> dataInBasketItem = new HashMap<>();
        dataInBasketItem.put("foodsInfo", foodDTO);
        batch.update(foodInBasketItemRef, dataInBasketItem);

        DocumentReference basketInBasketItemRef = db.collection("basketItems")
                .document(basketItemDTO.getBasketItemID());
        Map<String, Object> dataBasketInBasketItem = new HashMap<>();
        dataBasketInBasketItem.put("basketsInfo", basketDTO);
        batch.update(basketInBasketItemRef, dataBasketInBasketItem);


        return batch.commit();
    }

    public Task<Void> updateCart(CartDTO cartDTO, BasketDTO basketDTO, BasketItemDTO basketItemDTO, BasketItemDTO preBasketItemDTO, BasketDTO preBasketDTO) {
        DocumentReference docBasketItem = db.collection("basketItems")
                .document(basketItemDTO.getBasketItemID());
        DocumentReference docBasket = db.collection("baskets")
                .document(basketDTO.getBasketID());
        DocumentReference docCart = db.collection("carts")
                .document(cartDTO.getCartID());

        return db.runTransaction(new Transaction.Function<Void>() {
            @Nullable
            @Override
            public Void apply(@NonNull Transaction transaction) throws FirebaseFirestoreException {
                Map<String, Object> dataBasketItem = new HashMap<>();
                dataBasketItem.put("basketItemsInfo.quantity", basketItemDTO.getQuantity());
                dataBasketItem.put("basketItemsInfo.price", basketItemDTO.getPrice());
                transaction.update(docBasketItem, dataBasketItem);

                Map<String, Object> dataDeleteBasketItem = new HashMap<>();
                dataDeleteBasketItem.put("basketItemsInfo", FieldValue.arrayRemove(preBasketItemDTO));
                transaction.update(docBasket, dataDeleteBasketItem);

                Map<String, Object> dataUpdateBasketItem = new HashMap<>();
                dataUpdateBasketItem.put("basketItemsInfo", FieldValue.arrayUnion(basketItemDTO));
                transaction.update(docBasket, dataUpdateBasketItem);

                Map<String, Object> dataBasket = new HashMap<>();
                dataBasket.put("basketsInfo.basketQuantity", basketDTO.getBasketQuantity());
                dataBasket.put("basketsInfo.basketPrice", basketDTO.getBasketPrice());
                transaction.update(docBasket, dataBasket);

                Map<String, Object> dataDeleteBasket = new HashMap<>();
                dataDeleteBasket.put("basketsInfo", FieldValue.arrayRemove(preBasketDTO));
                transaction.update(docCart, dataDeleteBasket);

                Map<String, Object> dataUpdateBasket = new HashMap<>();
                dataUpdateBasket.put("basketsInfo", FieldValue.arrayUnion(basketDTO));
                transaction.update(docCart, dataUpdateBasket);

                return null;
            }
        });
    }


    public Task<Void> updateCartSameBasket(CartDTO cartDTO, BasketDTO basketDTO, BasketItemDTO basketItemDTO, BasketDTO preBasket, FoodDTO foodDTO) {
        DocumentReference docBasketItem = db.collection("basketItems")
                .document();
        DocumentReference docBasket = db.collection("baskets")
                .document(basketDTO.getBasketID());
        DocumentReference docCart = db.collection("carts")
                .document(cartDTO.getCartID());
        basketItemDTO.setBasketItemID(docBasketItem.getId());
        WriteBatch batch = db.batch();
        Map<String, Object> dataBasketItem = new HashMap<>();
        dataBasketItem.put("basketItemsInfo", basketItemDTO);
        batch.set(docBasketItem, dataBasketItem, SetOptions.merge());

        DocumentReference foodInBasketItemRef = db.collection("basketItems")
                .document(basketItemDTO.getBasketItemID());
        Map<String, Object> dataInBasketItem = new HashMap<>();
        dataInBasketItem.put("foodsInfo", foodDTO);
        batch.update(foodInBasketItemRef, dataInBasketItem);

        DocumentReference basketInBasketItemRef = db.collection("basketItems")
                .document(basketItemDTO.getBasketItemID());
        Map<String, Object> dataBasketInBasketItem = new HashMap<>();
        dataBasketInBasketItem.put("basketsInfo", basketDTO);
        batch.update(basketInBasketItemRef, dataBasketInBasketItem);

        batch.commit();
        return db.runTransaction(new Transaction.Function<Void>() {
            @Nullable
            @Override
            public Void apply(@NonNull Transaction transaction) throws FirebaseFirestoreException {
                Map<String, Object> dataUpdateBasketItem = new HashMap<>();
                dataUpdateBasketItem.put("basketItemsInfo", FieldValue.arrayUnion(basketItemDTO));
                dataUpdateBasketItem.put("basketsInfo.basketPrice", basketDTO.getBasketPrice());
                dataUpdateBasketItem.put("basketsInfo.basketQuantity", basketDTO.getBasketQuantity());
                transaction.update(docBasket, dataUpdateBasketItem);

                Map<String, Object> dataDeleteBasket = new HashMap<>();
                dataDeleteBasket.put("basketsInfo", FieldValue.arrayRemove(preBasket));
                transaction.update(docCart, dataDeleteBasket);

                Map<String, Object> dataUpdateBasket = new HashMap<>();
                dataUpdateBasket.put("basketsInfo", FieldValue.arrayUnion(basketDTO));
                transaction.update(docCart, dataUpdateBasket);

                return null;
            }
        });
    }

    public Task<Void> updateCartSameUser(CartDTO cartDTO, BasketDTO basketDTO, BasketItemDTO basketItemDTO, FoodDTO foodDTO, CartDTO preCart) {
        DocumentReference docBasketItem = db.collection("basketItems")
                .document();
        DocumentReference docBasket = db.collection("baskets")
                .document();
        DocumentReference docCart = db.collection("carts")
                .document(cartDTO.getCartID());
        basketItemDTO.setBasketItemID(docBasketItem.getId());
        basketDTO.setBasketID(docBasket.getId());
        WriteBatch batch = db.batch();
        //tao basketItem
        Map<String, Object> dataBasketItem = new HashMap<>();
        dataBasketItem.put("basketItemsInfo", basketItemDTO);
        batch.set(docBasketItem, dataBasketItem, SetOptions.merge());

        DocumentReference foodInBasketItemRef = db.collection("basketItems")
                .document(basketItemDTO.getBasketItemID());
        Map<String, Object> dataInBasketItem = new HashMap<>();
        dataInBasketItem.put("foodsInfo", foodDTO);
        batch.update(foodInBasketItemRef, dataInBasketItem);

        DocumentReference basketInBasketItemRef = db.collection("basketItems")
                .document(basketItemDTO.getBasketItemID());
        Map<String, Object> dataBasketInBasketItem = new HashMap<>();
        dataBasketInBasketItem.put("basketsInfo", basketDTO);
        batch.update(basketInBasketItemRef, dataBasketInBasketItem);

        //tao basket
        Map<String, Object> dataBasket = new HashMap<>();
        dataBasket.put("basketsInfo", basketDTO);
        batch.set(docBasket, dataBasket, SetOptions.merge());

        DocumentReference basketItemInBasketRef = db.collection("baskets")
                .document(basketDTO.getBasketID());
        Map<String, Object> dataInBasket = new HashMap<>();
        dataInBasket.put("basketItemsInfo", FieldValue.arrayUnion(basketItemDTO));
        batch.update(basketItemInBasketRef, dataInBasket);

        DocumentReference cartInBasketRef = db.collection("baskets")
                .document(basketDTO.getBasketID());
        Map<String, Object> dataCartInBasket = new HashMap<>();
        dataCartInBasket.put("cartsInfo", cartDTO);
        batch.update(cartInBasketRef, dataCartInBasket);

        batch.commit();
        return db.runTransaction(new Transaction.Function<Void>() {
            @Nullable
            @Override
            public Void apply(@NonNull Transaction transaction) throws FirebaseFirestoreException {
                Map<String, Object> dataUpdateBasket = new HashMap<>();
                dataUpdateBasket.put("basketsInfo", FieldValue.arrayUnion(basketDTO));
                transaction.update(docCart, dataUpdateBasket);
                return null;
            }
        });
    }

    public Task<Void> removeBasketItem(CartDTO cartDTO, BasketDTO basketDTO, BasketItemDTO preBasketItem, BasketDTO preBasket) {
        DocumentReference docBasketItem = db.collection("basketItems")
                .document(preBasketItem.getBasketItemID());
        DocumentReference docBasket = db.collection("baskets")
                .document(basketDTO.getBasketID());
        DocumentReference docCart = db.collection("carts")
                .document(cartDTO.getCartID());

        WriteBatch batch = db.batch();
        batch.delete(docBasketItem);
        batch.commit();

        return db.runTransaction(new Transaction.Function<Void>() {
            @Nullable
            @Override
            public Void apply(@NonNull Transaction transaction) throws FirebaseFirestoreException {
                Map<String, Object> dataDeleteBasketItem = new HashMap<>();
                dataDeleteBasketItem.put("basketItemsInfo", FieldValue.arrayRemove(preBasketItem));
                dataDeleteBasketItem.put("basketsInfo.basketPrice", basketDTO.getBasketPrice());
                dataDeleteBasketItem.put("basketsInfo.basketQuantity", basketDTO.getBasketQuantity());
                transaction.update(docBasket, dataDeleteBasketItem);

                Map<String, Object> dataDeleteBasket = new HashMap<>();
                dataDeleteBasket.put("basketsInfo", FieldValue.arrayRemove(preBasket));
                transaction.update(docCart, dataDeleteBasket);

                Map<String, Object> dataUpdateBasket = new HashMap<>();
                dataUpdateBasket.put("basketsInfo", FieldValue.arrayUnion(basketDTO));
                transaction.update(docCart, dataUpdateBasket);
                return null;
            }
        });
    }

    public Task<QuerySnapshot> getCartByUserID(String uid) {
        return db.collection("carts")
                .whereEqualTo("cartsInfo.userInfo.userID", uid)
                .get();
    }


    public Task<Void> removeAfterOrder(String basketItemID) {
        return db.collection("basketItems").document(basketItemID).delete();
    }

    public Task<Void> removeBasket(String basketID) {
        return db.collection("baskets").document(basketID).delete();
    }

    public Task<Void> updateCartAfterOrder(CartDTO cartDTO, BasketDTO basketDTO) {
        DocumentReference docCart = db.collection("carts")
                .document(cartDTO.getCartID());
        return db.runTransaction(new Transaction.Function<Void>() {
            @Nullable
            @Override
            public Void apply(@NonNull Transaction transaction) throws FirebaseFirestoreException {
                Map<String, Object> dataDeleteBasket = new HashMap<>();
                dataDeleteBasket.put("basketsInfo", FieldValue.arrayRemove(basketDTO));
                transaction.update(docCart, dataDeleteBasket);
                return null;
            }
        });
    }
}
