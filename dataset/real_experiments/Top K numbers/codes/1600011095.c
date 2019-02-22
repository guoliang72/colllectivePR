#include<stdio.h>
int main()
{
	int a[1000],i,j,k,n,t;
	scanf("%d\n",&n);
	for(i=0;i<n;i=i+1)
	{
		scanf("%d",&a[i]);
	}
	scanf("%d",&k);
	for(i=0;i<n;i=i+1)
	{
		for(j=0;j<n-i-1;j=j+1)
		{
			if(a[i]<a[i+1])
			{
				t=a[i];
				a[i]=a[i+1];
				a[i+1]=t;
			}
		}
	}
	for(i=k-1;i>=0;i=i-1)		//循环反了
	{
		printf("%d\n",a[i]);
	}
	return 0;
}