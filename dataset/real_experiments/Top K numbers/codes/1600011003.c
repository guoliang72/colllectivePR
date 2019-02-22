#include<stdio.h>

int main(){
	int n,k,i,j,temp;
	int a[1000];
	scanf("%d\n",&n);
	for(i=0;i<=n;i=i+1)		// 边界错
		scanf("%d",&a[i]);
	scanf("\n");		// 多余
	scanf("%d",&k);
	for(i=0;i<n;i=i+1){		// 边界错 i<n-1
		for(j=0;j<i;j=i+1){	// 边界错 j<n-1-i next错
			if(a[j]<a[j+1]){
				temp=a[j];
				a[j]=a[j+1];
				a[j+1]=temp;}
		}}
	for(i=0;i<k;i=i+1)
		printf("%d\n",a[i]);
	return 0;
}