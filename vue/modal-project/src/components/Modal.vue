<template>
    <div class="backdrop">
        <div class="modal" :class="{ sale: theme === 'sale' }" >
            <h1>{{header}}</h1>
            <p>{{title}}</p>
     </div>
    </div>
</template>

<script>
    export default {
        props: ['header', 'title', 'theme']            
        }
</script>

<style >

.modal{
    width: 400px;
    padding: 20px;
    margin: 100px auto;
    background: white;
    border-radius: 10px;
    align-self: center;
}

.backdrop{
    top:0;
    position: fixed;
    background: rgba(0, 0, 0, 0.5);
    width: 100%;
    height: 100%;
}

.modal h1 {
    background: green;
    border: none;
    padding: 0;
}

.modal.sale {
    background: chartreuse;
    color: wheat;

}

.modal.sale h1 {
    color: lemonchiffon;

}
</style>